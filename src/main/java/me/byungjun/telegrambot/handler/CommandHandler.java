package me.byungjun.telegrambot.handler;

import me.byungjun.telegrambot.domain.BotMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommandHandler {

    public static BotMode mode = BotMode.NONE;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private CrawlingHandler crawlingHandler;

    private static String category = "tmovie";
    private static String bestCategory = "Week";

    // commend
    static final String SEARCH_TORRENT = "토렌트 키워드 검색";
    static final String TORRENT_BEST = "장르별 베스트 보기";
    static final String SHOW_STATUS = "다운로드 목록";
    static final String HOME_BACK = "처음으로";

    static final String MOVIE = "영화";
    static final String DRAMA = "드라마";
    static final String ENT = "예능";
    static final String TV = "TV";
    static final String ANI = "애니메이션";
    static final String MUSIC = "음악";

    static final String DAILY = "일간 베스트";
    static final String WEEKLY = "주간 베스트";
    static final String MONTHLY = "월간 베스트";

    public SendMessage resolveCommand(long chatId, String text) {
        SendMessage message = new SendMessage().setChatId(chatId);

        switch (text) {
            case SEARCH_TORRENT:
                message = createMessageButton(chatId, "다운받으실 장르를 선택하세요.",
                        MOVIE, DRAMA, ENT, TV, ANI, MUSIC);
                mode = BotMode.INPUT_KEYWORD;
                break;
            case TORRENT_BEST:
                message = createMessageButton(chatId, "장르를 선택하세요.",
                        MOVIE, DRAMA, ENT, ANI);
                mode = BotMode.CHOOSE_BEST;
                break;
            case HOME_BACK:
                message = goHome(chatId, "선택하세요.");
                mode = BotMode.NONE;
                break;
            case SHOW_STATUS:
                message = goHome(chatId, messageHandler.list()).enableMarkdown(false);
                mode = BotMode.NONE;
                break;
            default:
                switch (mode) {
                    case INPUT_KEYWORD:
                        message = categorySearch(chatId, text);
                        break;
                    case CHOOSE:
                        message = goHome(chatId, messageHandler.selectedOne(text));
                        break;
                    case CHOOSE_BEST:
                        message = categoryBest(chatId, text);
                        mode = BotMode.INPUT_BEST_TORRENT;
                        break;
                    case INPUT_BEST_TORRENT:
                        message = categoryBestChoice(chatId, text, message);
                        mode = BotMode.CHOOSE;
                        break;
                    default:
                        message = goHome(chatId, "선택하세요.");
                        break;
                }
                break;
        }
        return message;
    }

    private SendMessage categoryBestChoice(long chatId, String text, SendMessage message) {
        String[] msg;
        String inlineMsg;
        switch (text) {
            case DAILY:
                msg = crawlingHandler.dailyBest(category).toArray(new String[0]);
                inlineMsg = parseList(msg);
                message = createMessageButton(chatId, inlineMsg, msg);
                break;
            case WEEKLY:
                bestCategory = "Week";
                msg = crawlingHandler.weekMonthBest(category, bestCategory).toArray(new String[0]);
                inlineMsg = parseList(msg);
                message = createMessageButton(chatId, inlineMsg, msg).enableMarkdown(false);
                break;
            case MONTHLY:
                bestCategory = "Month";
                msg = crawlingHandler.weekMonthBest(category, bestCategory).toArray(new String[0]);
                inlineMsg = parseList(msg);
                message = createMessageButton(chatId, inlineMsg, msg).enableMarkdown(false);
                break;
            default:
                message = goHome(chatId, "잘못 선택하셨습니다.");
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

    private SendMessage goHome(long chatId, String msg) {
        SendMessage message;
        message = createMessageButton(chatId, msg, SEARCH_TORRENT, TORRENT_BEST, SHOW_STATUS);
        return message;
    }

    private SendMessage categorySearch(long chatId, String text) {
        switch (text) {
            case MOVIE:
                category = "tmovie";
                break;
            case DRAMA:
                category = "tdrama";
                break;
            case ENT:
                category = "tent";
                break;
            case TV:
                category = "tv";
                break;
            case ANI:
                category = "tani";
                break;
            case MUSIC:
                category = "tmusic";
                break;
            default:
                String[] msg = crawlingHandler.getSearch(text, category).toArray(new String[0]);
                String inlineMsg = parseList(msg);
                SendMessage message = createMessageButton(chatId, inlineMsg, msg);
                return message;
        }
        return new SendMessage().setChatId(chatId).setText("선택하셨습니다. 검색할 키워드를 입력해주세요.");
    }

    private SendMessage categoryBest(long chatId, String text) {
        switch (text) {
            case MOVIE:
                category = "tmovie";
                break;
            case DRAMA:
                category = "tdrama";
                break;
            case ENT:
                category = "tent";
                break;
            case ANI:
                category = "tani";
                break;
            default:
                return goHome(chatId, "잘못 입력하셨습니다.");
        }
        return createMessageButton(chatId, "선택 해주세요.", DAILY, WEEKLY, MONTHLY);
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