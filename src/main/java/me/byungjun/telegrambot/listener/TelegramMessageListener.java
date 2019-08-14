package me.byungjun.telegrambot.listener;

import me.byungjun.telegrambot.handler.CommandHandler;
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

@Component
public class TelegramMessageListener {

    @Autowired
    private CommandHandler commandHandler;

    @Value("${telegram.bot.name}")
    private String telegramBotName;

    @Value("${telegram.bot.key}")
    private String telegramBotKey;

    private long userId = 0;

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
                        if (userId == 0) {
                            userId = id;
                        }
                        if (userId != id) {
                            System.out.println("유저가 틀려");
                            return;
                        }
                        long chatId = update.getMessage().getChatId();
                        String stringMessage = update.getMessage().getText();
                        System.out.println(stringMessage + ", " + userId);

                        SendMessage message = commandHandler.resolveCommand(chatId, stringMessage);

                        try {
                            execute(message); // Call method to send the message
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        try {
                            clearWebhook();
                        } catch (TelegramApiRequestException e) {
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
