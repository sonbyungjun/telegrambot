package me.byungjun.telegrambot.handler;

import me.byungjun.telegrambot.domain.BotMode;
import me.byungjun.telegrambot.domain.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CrawlingHandler {

    @Value("${downloadURL}")
    String downloadURL;

    @Autowired
    MessageHandler messageHandler;

    public static List<Content> contents;

    public void getMagnet(Content content) {
        String URL = downloadURL + content.getLink();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elem = doc.select("#external-frame");
            Document iframeDoc = Jsoup.connect(downloadURL + elem.attr("src")).get();
            Elements iframeElem = iframeDoc.select(".torrent_magnet");
            messageHandler.create(iframeElem.select("a").text());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSearch(String stringMessage, String category) {
        String mms = "";
        String URL = downloadURL + "board.php?b_id=" + category + "&mode=list&sc=" + stringMessage;
        List<String> list = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elem = doc.select(".b_list > tbody > tr");
            contents = new ArrayList<Content>();

            for (int i = 0; i < elem.size(); i++) {
                String no = elem.eq(i).select(".num").text();
                String title = elem.eq(i).select(".subject").text();
                String dateTime = elem.eq(i).select(".datetime").text();
                String link = elem.eq(i).select(".subject > .list_subject > a:nth-child(2)").attr("href");

                Content content = new Content().builder()
                        .no(no)
                        .title(title)
                        .link(link)
                        .build();

                if (no.contains("notice")) {
                    continue;
                }
                contents.add(content);
                mms = no + ". " + title + "\n업로드날짜: " + dateTime + "\n\n";
                list.add(mms);
            }

            CommandHandler.mode = BotMode.CHOOSE;

        } catch (IOException e) {
            e.printStackTrace();
            mms = "오류!";
            list.add(mms);
            CommandHandler.mode = BotMode.NONE;
            return list;
        }

        if (mms.isEmpty()) {
            mms = "찾는게 없다.";
            list.add(mms);
            CommandHandler.mode = BotMode.NONE;
            return list;
        }
        mms = "다운로드 받으실 번호를 입력해주세요.";
        list.add(mms);
        return list;
    }
}