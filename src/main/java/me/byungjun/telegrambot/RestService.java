package me.byungjun.telegrambot;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class RestService {

    @Value("${nas.down.url}")
    private String downUrl;

    @Value("${nas.username}")
    private String username;

    @Value("${nas.password}")
    private String password;

    @Autowired
    RestTemplate restTemplate;

    public void create(String magnet) {
        URI uri = null;
        try {
            uri = new URI(downUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RequestEntity<String> requestEntity = RequestEntity.post(uri)
                .header("Cookie", "id=" + loginNas())
                .body("api=SYNO.DownloadStation.Task&version=1&method=create&uri=" + magnet);

        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        System.out.println(response.getBody().toString());
    }

    public String loginNas() {
        String response = restTemplate.getForObject(
                "http://saint2030.synology.me:5000/webapi/auth.cgi?api=SYNO.API.Auth&version=2&method=login&account=" + username + "&passwd=" + password + "&session=DownloadStation&format=sid",
                String.class);
        JSONObject res = new JSONObject(response);
        JSONObject data = (JSONObject) res.get("data");
        return (String) data.get("sid");
    }

    public String list() {
        URI uri = null;
        try {
            uri = new URI("http://saint2030.synology.me:5000/webapi/DownloadStation/task.cgi?api=SYNO.DownloadStation.Task&version=1&method=list&additional=detail,file");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header("Cookie", "id=" + loginNas())
                .build();
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        JSONObject res = new JSONObject(response.getBody().toString());
        JSONObject data = (JSONObject) res.get("data");
        JSONArray tasks = (JSONArray) data.get("tasks");
        String message = "";

        if (tasks.length() < 0) {
            message = "목록이 없습니다.";
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

}
