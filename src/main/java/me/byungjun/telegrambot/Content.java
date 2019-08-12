package me.byungjun.telegrambot;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Content {
    private String no;
    private String title;
    private String link;
}
