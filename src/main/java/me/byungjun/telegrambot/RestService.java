package me.byungjun.telegrambot;

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

}
