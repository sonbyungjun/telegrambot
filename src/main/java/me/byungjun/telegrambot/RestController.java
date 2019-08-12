package me.byungjun.telegrambot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Controller
public class RestController {

    @Value("${nas.down.url}")
    private String downUrl;

    @Value("${nas.username}")
    private String username;

    @Value("${nas.password}")
    private String password;

    public void magnetDown(String magnet) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> values = new LinkedMultiValueMap<>();
        values.add("api", "SYNO.DownloadStation.Task");
        values.add("version", "1");
        values.add("method", "create");
        values.add("uri", magnet);
        values.add("username", username);
        values.add("password", password);
        String response = restTemplate.postForObject(
                downUrl,
                "api=SYNO.DownloadStation.Task&version=1&method=create&uri=" + magnet + "&username=byungjun&password=qudwnsl1",
                String.class);
        System.out.println(response);
    }

}
