package me.byungjun.telegrambot.domain;

import lombok.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    int id;
    boolean active = false;
    BotMode mode = BotMode.NONE;
    List<Content> contents;
}