package com.example.todolistbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ToDoListBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToDoListBotApplication.class, args);
    }
}