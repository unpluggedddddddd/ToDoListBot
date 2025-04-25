package com.example.todolistbot.service;

import com.example.todolistbot.bot.ToDoListBot;
import com.example.todolistbot.entity.Task;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@EnableScheduling
public class ReminderService {

    private final TaskService taskService;
    private final ToDoListBot bot;

    public ReminderService(TaskService taskService, ToDoListBot bot) {
        this.taskService = taskService;
        this.bot = bot;
    }

    @Scheduled(fixedRate = 60000) // Проверять каждую минуту
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(1); // Напоминание за 1 час
        List<Task> tasks = taskService.findTasksWithUpcomingDeadline(now, threshold);

        for (Task task : tasks) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(task.getUserId()));
            message.setText(String.format("Напоминание: задача '%s' должна быть выполнена к %s!",
                    task.getTitle(), task.getDeadline().format(taskService.getFormatter())));
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}