package me.byungjun.telegrambot.handler;

import me.byungjun.telegrambot.domain.BotMode;
import me.byungjun.telegrambot.domain.Content;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {

    @Autowired
    private NasApiHandler nasApiHandler;

    @Autowired
    private CrawlingHandler crawlingHandler;

    public void create(String magnet) {
        ResponseEntity<String> response = nasApiHandler.createTorrent(magnet);
        System.out.println(response.getBody());
    }

    public String list() {
        ResponseEntity<String> response = nasApiHandler.findByAllTorrentList();
        JSONObject res = new JSONObject(response.getBody());
        JSONObject data = (JSONObject) res.get("data");
        JSONArray tasks = (JSONArray) data.get("tasks");
        String message = "";

        if (tasks.isEmpty()) {
            message = "목록이 없습니다.";
            return message;
        }

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject json = tasks.getJSONObject(i);
            JSONObject additional = json.getJSONObject("additional");
            String status = "";
            long fileSize = json.getLong("size");
            String fileParse = "파일들 : \n";

            if (json.get("status").equals("downloading")) {
                JSONArray files = additional.getJSONArray("file");
                long sizeDown = 0L;
                for (int j = 0; j < files.length(); j++) {
                    JSONObject file = (JSONObject) files.get(j);
                    fileParse += file.get("filename") + "\n";
                    sizeDown += file.getLong("size_downloaded");
                }
                double sizeParse = (double) sizeDown / 1e+9;
                double percentage = (double) sizeDown / (double) fileSize * 100;
                status = String.valueOf((int) percentage) + "% " + "(" + String.format("%.2f", sizeParse) + " GB)";
            } else {
                status = "다운로드 완료";
            }

            message += "고유 : " + json.getString("id") + "\n" +
                       "제목 : " + json.getString("title") + "\n" +
                       "총용량 : " + String.format("%.2f", (double) fileSize / 1e+9) + " GB\n" +
                       "진행상황 : " + status + "\n" +
                       (fileParse.length() > 7 ? fileParse + "\n" : "\n");
        }
        return message;
    }

    public String selectedOne(String text) {
        String message = "없는 번호이거나 잘못된 입력입니다.";
        Content content = null;
        for (Content c : CrawlingHandler.contents) {
            if (c.getNo().equals(text.substring(0, text.indexOf(".")))) {
                content = c;
                message = content.getTitle() + "\n다운로드 시작합니다.";
                CommandHandler.mode = BotMode.NONE;
                break;
            }
        }
        if (content == null) {
            message = "잘못된 입력입니다.\n번호를 입력해주세요.";
            return message;
        }
        crawlingHandler.getMagnet(content);

        return message;
    }
}
