package me.byungjun.telegrambot.handler;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Controller
public class NasApiHandler {

    @Value("${nas.down.url}")
    private String downUrl;

    @Value("${nas.username}")
    private String username;

    @Value("${nas.password}")
    private String password;

    @Autowired
    RestTemplate restTemplate;

    public ResponseEntity<String> createTorrent(String magnet) {
        URI uri = null;
        try {
            uri = new URI(downUrl + "webapi/DownloadStation/task.cgi");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RequestEntity<String> requestEntity = RequestEntity.post(uri)
                .header("Cookie", "id=" + loginNas())
                .body("api=SYNO.DownloadStation.Task&version=1&method=create&uri=" + magnet);
        return restTemplate.exchange(requestEntity, String.class);
    }

    public String loginNas() {
        String response = restTemplate.getForObject(
                downUrl + "webapi/auth.cgi?api=SYNO.API.Auth&version=2&method=login&account=" + username + "&passwd=" + password + "&session=DownloadStation&format=sid",
                String.class);
        JSONObject res = new JSONObject(response);
        JSONObject data = (JSONObject) res.get("data");
        return (String) data.get("sid");
    }

    public ResponseEntity<String> findByAllTorrentList() {
        URI uri = null;
        try {
            uri = new URI(downUrl + "webapi/DownloadStation/task.cgi?api=SYNO.DownloadStation.Task&version=1&method=list&additional=detail,file");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header("Cookie", "id=" + loginNas())
                .build();
        return restTemplate.exchange(requestEntity, String.class);
    }
    
}