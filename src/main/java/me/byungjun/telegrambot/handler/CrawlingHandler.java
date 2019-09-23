package me.byungjun.telegrambot.handler;

import me.byungjun.telegrambot.domain.BotMode;
import me.byungjun.telegrambot.domain.Content;
import me.byungjun.telegrambot.domain.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    public void getMagnet(Content content) {
        String URL = downloadURL + content.getLink();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elem = doc.select("#main_body > table > tbody > tr > td > a");
            messageHandler.create(elem.text());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSearch(String stringMessage, User user) {
        String mms = "";
        String URL = downloadURL + "bbs/s.php?k=" + stringMessage;
        return lists(mms, URL, user);
    }

    private List<String> lists(String mms, String URL, User user) {
        List<String> list = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            mms = parseListing(mms, list, doc, user);

            user.setMode(BotMode.CHOOSE);

        } catch (IOException e) {
            e.printStackTrace();
            mms = "오류!";
            list.add(mms);
            user.setMode(BotMode.NONE);
            return list;
        }

        if (mms.isEmpty()) {
            mms = "찾는게 없습니다.";
            list.add(mms);
            user.setMode(BotMode.NONE);
            return list;
        }

        return list;
    }

    private String parseListing(String mms, List<String> list, Document doc, User user) {
        Elements elem = doc.select(".board_list > tbody > tr");

        List<Content> contents = new ArrayList<Content>();

        int size = elem.size();
        if (size > 22) {
            size = 22;
        }

        for (int i = 0; i < size; i++) {
            String no = String.valueOf(i + 1);
            String title = elem.eq(i).select(".subject").text();
            if (title.contains("마그넷")) {
                continue;
            }
            String dateTime = elem.eq(i).select(".datetime").text();
            String link = elem.eq(i).select(".subject > a[target]").attr("href");
            if (link.length() > 3) {
                link = elem.eq(i).select(".subject > a[target]").attr("href").substring(3);
            }
            String fileSize = elem.eq(i).select(".hit").text();

            Content content = new Content().builder()
                    .no(no)
                    .title(title)
                    .link(link)
                    .build();

            if (no.contains("notice")) {
                continue;
            }
            contents.add(content);
            mms = no + ". " + title + "\n업로드날짜: " + dateTime + "\n파일용량: " + fileSize + "\n\n";
            list.add(mms);
        }
        user.setContents(contents);
        return mms;
    }

    public List<String> best(String type, User user) {
        String mms = "";
        String URL = downloadURL + "bbs/popular.html";
        List<String> list = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements fields = doc.select("#fieldset_list > fieldset");

            Element elem = null;
            for (Element e : fields) {
                if (e.select("legend > a").text().equals(type)) {
                    elem = e;
                    break;
                }
            }
            if (elem == null) {
                mms = "찾는게 없습니다.";
                list.add(mms);
                user.setMode(BotMode.NONE);
                return list;
            }
            List<Content> contents = new ArrayList<Content>();

            Elements aTags = elem.select("tbody > tr > td > a");

            for (int i = 0; i < aTags.size(); i++) {
                String no = String.valueOf(i + 1);
                String title = aTags.eq(i).text();
                String link = aTags.eq(i).attr("href").substring(3);

                Content content = new Content().builder()
                        .no(no)
                        .title(title)
                        .link(link)
                        .build();

                contents.add(content);
                mms = no + ". " + title + "\n\n";
                list.add(mms);
            }
            user.setContents(contents);
            user.setMode(BotMode.CHOOSE);

        } catch (IOException e) {
            e.printStackTrace();
            mms = "오류!";
            list.add(mms);
            user.setMode(BotMode.NONE);
            return list;
        }
        if (mms.isEmpty()) {
            mms = "찾는게 없습니다.";
            list.add(mms);
            user.setMode(BotMode.NONE);
            return list;
        }
        return list;
    }

}
