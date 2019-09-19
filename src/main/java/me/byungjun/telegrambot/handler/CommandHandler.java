package me.byungjun.telegrambot.handler;

import me.byungjun.telegrambot.domain.BotMode;
import me.byungjun.telegrambot.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommandHandler {

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private CrawlingHandler crawlingHandler;


    // commend
    static final String SEARCH_TORRENT = "토렌트 키워드 검색";
    static final String TORRENT_BEST = "장르별 베스트 보기";
    static final String SHOW_STATUS = "다운로드 목록";
    static final String HOME_BACK = "처음으로";

    static final String MOVIE = "영화";
    static final String DRAMA = "TV드라마";
    static final String ENT = "TV예능";
    static final String TV = "도서/만화";
    static final String ANI = "애니메이션";
    static final String MUSIC = "해외음원";

    public SendMessage resolveCommand(long chatId, String text, User user) {
        SendMessage message = new SendMessage().setChatId(chatId);
        BotMode mode = user.getMode();
        switch (text) {
            case SEARCH_TORRENT:
                message = new SendMessage().setChatId(chatId).setText("선택하셨습니다. 검색할 키워드를 입력해주세요.");
                user.setMode(BotMode.INPUT_KEYWORD);
                break;
            case TORRENT_BEST:
                message = createMessageButton(chatId, "장르를 선택하세요.",
                        MOVIE, DRAMA, ENT, ANI, TV, MUSIC);
                user.setMode(BotMode.CHOOSE_BEST);
                break;
            case HOME_BACK:
                message = goHome(chatId, "선택하세요.");
                user.setMode(BotMode.NONE);
                break;
            case SHOW_STATUS:
                message = goHome(chatId, messageHandler.list()).enableMarkdown(false);
                user.setMode(BotMode.NONE);
                break;
            default:
                switch (mode) {
                    case INPUT_KEYWORD:
                        String[] msg = crawlingHandler.getSearch(text, user).toArray(new String[0]);
                        String inlineMsg = parseList(msg);
                        message = createMessageButton(chatId, inlineMsg, msg);
                        break;
                    case CHOOSE:
                        message = goHome(chatId, messageHandler.selectedOne(text, user));
                        break;
                    case CHOOSE_BEST:
                        message = categoryBest(chatId, text, message, user);
                        user.setMode(BotMode.INPUT_BEST_TORRENT);
                        break;
                    case INPUT_BEST_TORRENT:
                        message = goHome(chatId, messageHandler.selectedOne(text, user));
                        user.setMode(BotMode.CHOOSE);
                        break;
                    default:
                        message = goHome(chatId, "선택하세요.");
                        break;
                }
                break;
        }
        return message;
    }

    private String parseList(String[] msg) {
        String inlineMsg = "";
        for (String s : msg) {
            inlineMsg += s;
        }
        return inlineMsg;
    }

    public SendMessage goHome(long chatId, String msg) {
        SendMessage message;
        message = createMessageButton(chatId, msg, SEARCH_TORRENT, TORRENT_BEST, SHOW_STATUS);
        return message;
    }



    private SendMessage categoryBest(long chatId, String text, SendMessage message, User user) {
        String[] msg;
        String inlineMsg;
        switch (text) {
            case MOVIE:
                msg = crawlingHandler.best(MOVIE, user).toArray(new String[0]);
                break;
            case DRAMA:
                msg = crawlingHandler.best(DRAMA, user).toArray(new String[0]);
                break;
            case ENT:
                msg = crawlingHandler.best(ENT, user).toArray(new String[0]);
                break;
            case ANI:
                msg = crawlingHandler.best(ANI, user).toArray(new String[0]);
                break;
            case TV:
                msg = crawlingHandler.best(TV, user).toArray(new String[0]);
                break;
            case MUSIC:
                msg = crawlingHandler.best(MUSIC, user).toArray(new String[0]);
                break;
            default:
                return goHome(chatId, "잘못 입력하셨습니다.");
        }
        inlineMsg = parseList(msg);
        message = createMessageButton(chatId, inlineMsg, msg).enableMarkdown(false);
        return message;
    }

    private SendMessage createMessageButton(long chatId, String message, String... messages) {
        List<KeyboardRow> keyboardRow = new ArrayList<>();
        for (String s : messages) {
            KeyboardRow row = new KeyboardRow();
            row.add(s);
            keyboardRow.add(row);
        }
        if (!messages[0].equals(SEARCH_TORRENT)) {
            KeyboardRow row = new KeyboardRow();
            row.add(HOME_BACK);
            keyboardRow.add(row);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setKeyboard(keyboardRow)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage().setChatId(chatId)
                .enableMarkdown(true)
                .enableWebPagePreview()
                .setReplyToMessageId(0)
                .setReplyMarkup(replyKeyboardMarkup)
                .setText(message);

        return sendMessage;
    }

}