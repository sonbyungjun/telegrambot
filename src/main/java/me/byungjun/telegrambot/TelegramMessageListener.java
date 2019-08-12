package me.byungjun.telegrambot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramMessageListener {

    @Value("${telegram.bot.name}")
    private String telegramBotName;

    @Value("${telegram.bot.key}")
    private String telegramBotKey;

    private long userId = 0;
    private BotMode mode = BotMode.NONE;

    // commend
    private static final String SEARCH_TORRENT = "토렌트 검색";
    private static final String DELETE_DONE_TORRENT = "토렌트 삭제";
    private static final String SHOW_STATUS = "다운로드 목록";

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
                    System.out.println("호출됨");
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

                        SendMessage message = resolveCommand(chatId, stringMessage);

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

    private SendMessage resolveCommand(long chatId, String text) {
        SendMessage message = new SendMessage().setChatId(chatId);

        switch (text) {
            case SEARCH_TORRENT:
                message.setText("검색어를 입력하세요.");
                mode = BotMode.INPUT_KEYWORD;
                break;
            case DELETE_DONE_TORRENT:
                message.setText("번호를 입력하세요.");
                mode = BotMode.INPUT_DELETE_DONE_NUMBER;
                break;
            case SHOW_STATUS:
//                showStatus();
                mode = BotMode.NONE;
                break;
            default:
                switch (mode) {
                    case INPUT_KEYWORD:
                        message.setText(getSearch(text));
                        break;
                    case CHOOSE:
//                        selectedOne(text);
                        break;
                    case INPUT_DELETE_DONE_NUMBER:
//                        deleteTorrent(text);
                        break;
                    default:
                        message = sendMayIHelpYou(chatId);
                        break;
                }
                break;
        }

        return message;
    }


    private String getSearch(String stringMessage) {
        String URL = "https://www.tfreeca3.com/board.php?b_id=tmovie&mode=list&sc=" + stringMessage;
        String mms = "";
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements elem = doc.select(".b_list > tbody > tr");

            for (int i = 0; i < elem.size(); i++) {
                if (elem.eq(i).select(".num").text().contains("notice")) {
                    continue;
                }
                mms += elem.eq(i).select(".num").text() + "\n" + elem.eq(i).select(".subject").text() + "\n" + elem.eq(i).select(".datetime").text() + "\n\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
            mms = "오류!";
            return mms;
        }

        if (mms.isEmpty()) {
            mms = "찾는게 없다.";
        }

        mode = BotMode.CHOOSE;
        return mms;
    }

    private SendMessage sendMayIHelpYou(long chatId) {
        KeyboardRow row = new KeyboardRow();
        row.add(SEARCH_TORRENT);
        row.add(DELETE_DONE_TORRENT);
        row.add(SHOW_STATUS);

        List<KeyboardRow> keyboardrows = new ArrayList<>();
        keyboardrows.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setKeyboard(keyboardrows);

        SendMessage message = new SendMessage().setChatId(chatId)
                .enableMarkdown(true)
                .enableWebPagePreview()
                .setReplyToMessageId(0)
                .setReplyMarkup(replyKeyboardMarkup.setOneTimeKeyboard(true))
                .setText("선택해라");
        return message;
    }

}
