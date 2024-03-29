package me.byungjun.telegrambot.listener;

import me.byungjun.telegrambot.domain.BotMode;
import me.byungjun.telegrambot.domain.User;
import me.byungjun.telegrambot.handler.CommandHandler;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramMessageListener {

    @Autowired
    private CommandHandler commandHandler;

    @Value("${telegram.bot.name}")
    private String telegramBotName;

    @Value("${telegram.bot.key}")
    private String telegramBotKey;

    private static Map<Integer, User> users = new HashMap<>();

    @Value("${password}")
    private String password;

    @PostConstruct
    private void init() {
        ApiContextInitializer.init();
        TelegramBotsApi api = new TelegramBotsApi();

        try {
            api.registerBot(new TelegramLongPollingBot() {
                @Override
                public String getBotToken() {
                    return telegramBotKey;
                }

                @Override
                public void onUpdateReceived(Update update) {
                    if (update.hasMessage() && update.getMessage().hasText()) {

                        int id = update.getMessage().getFrom().getId();

                        long chatId = update.getMessage().getChatId();
                        String stringMessage = update.getMessage().getText();
                        System.out.println(stringMessage + ", " + id);

                        SendMessage message;

                        if (users.get(id) == null) {
                            users.put(id, User.builder().id(id).mode(BotMode.NONE).build());
                            message = new SendMessage(chatId, "비밀번호를 입력해주세요.");
                        } else if (!users.get(id).isActive()) {
                            if (stringMessage.equals(password)) {
                                users.get(id).setActive(true);
                                message = commandHandler.goHome(chatId, "사용자가 추가 되었습니다. 선택하세요.");
                            } else {
                                message = new SendMessage(chatId, "비밀번호가 틀렸습니다.\n 다시 입력해주세요.");
                            }
                        } else {
                            try {
                                message = commandHandler.resolveCommand(chatId, stringMessage, users.get(id));
                            } catch (JSONException e) {
                                message = new SendMessage(chatId, "NAS의 아이디나 비번을 확인해주세요.");
                                e.printStackTrace();
                            }
                        }

                        try {
                            execute(message); // Call method to send the message
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public String getBotUsername() {
                    return telegramBotName;
                }
            });
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }



}
