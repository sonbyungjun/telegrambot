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
    private NasTorrentHandler nasTorrentHandler;

    @Autowired
    private CrawlingHandler crawlingHandler;

    // commend
    static final String SEARCH_TORRENT = "토렌트 검색";
    static final String DELETE_DONE_TORRENT = "토렌트 삭제";
    static final String SHOW_STATUS = "다운로드 목록";

    public SendMessage resolveCommand(long chatId, String text) {
        SendMessage message = new SendMessage().setChatId(chatId);

        switch (text) {
            case SEARCH_TORRENT:
                message.setText("검색어를 입력하세요.");
                mode = BotMode.INPUT_KEYWORD;
                break;
            case DELETE_DONE_TORRENT:
                message.setText("고유번호를 입력하세요.");
                mode = BotMode.INPUT_DELETE_DONE_NUMBER;
                break;
            case SHOW_STATUS:
                message.setText(nasTorrentHandler.list());
                mode = BotMode.NONE;
                break;
            default:
                switch (mode) {
                    case INPUT_KEYWORD:
                        message.setText(crawlingHandler.getSearch(text));
                        break;
                    case CHOOSE:
                        message.setText(nasTorrentHandler.selectedOne(text));
                        break;
                    case INPUT_DELETE_DONE_NUMBER:
//                        String[] texts = {text};
//                        message.setText(restService.delete(texts));
                        message.setText("지원하지 않는 기능입니다.");
                        mode = BotMode.NONE;
                        break;
                    default:
                        message = sendMayIHelpYou(chatId);
                        break;
                }
                break;
        }

        return message;
    }

    private SendMessage sendMayIHelpYou(long chatId) {
        KeyboardRow row = new KeyboardRow();
        row.add(CommandHandler.SEARCH_TORRENT);
//        row.add(Command.DELETE_DONE_TORRENT);
        row.add(CommandHandler.SHOW_STATUS);

        List<KeyboardRow> keyboardRow = new ArrayList<>();
        keyboardRow.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup()
                .setKeyboard(keyboardRow);

        SendMessage message = new SendMessage().setChatId(chatId)
                .enableMarkdown(true)
                .enableWebPagePreview()
                .setReplyToMessageId(0)
                .setReplyMarkup(replyKeyboardMarkup.setOneTimeKeyboard(true))
                .setText("선택해라");
        return message;
    }
}