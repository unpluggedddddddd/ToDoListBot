package com.example.todolistbot.bot;

import com.example.todolistbot.entity.Task;
import com.example.todolistbot.service.TaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToDoListBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final TaskService taskService;

    public ToDoListBot(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = new SendMessage();

        // Обработка текстовых сообщений
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            message.setChatId(String.valueOf(chatId));

            if (messageText.equals("/start")) {
                message.setText("Привет! Я ToDoListBot. Используй команды:\n" +
                        "/add <название> | <описание> | <дедлайн в формате ГГГГ-ММ-ДДTЧЧ:ММ> - добавить задачу\n" +
                        "/list - показать задачи\n" +
                        "/done - отметить задачу выполненной\n" +
                        "/delete - удалить задачу");
            } else if (messageText.startsWith("/add")) {
                handleAddTask(messageText, userId, message);
            } else if (messageText.equals("/list")) {
                handleListTasks(userId, message, null);
            } else if (messageText.equals("/done")) {
                handleShowTasksForAction(userId, message, "done");
            } else if (messageText.equals("/delete")) {
                handleShowTasksForAction(userId, message, "delete");
            } else {
                message.setText("Неизвестная команда. Используй /start для списка команд.");
            }
        }

        // Обработка callback'ов от inline-кнопок
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            message.setChatId(String.valueOf(chatId));

            if (callbackData.startsWith("done_")) {
                Long taskId = Long.parseLong(callbackData.replace("done_", ""));
                taskService.markTaskAsCompleted(taskId);
                message.setText("Задача ID " + taskId + " отмечена выполненной.");
            } else if (callbackData.startsWith("delete_")) {
                Long taskId = Long.parseLong(callbackData.replace("delete_", ""));
                taskService.deleteTask(taskId);
                message.setText("Задача ID " + taskId + " удалена.");
            } else if (callbackData.equals("all") || callbackData.equals("completed") || callbackData.equals("not_completed")) {
                long userId = update.getCallbackQuery().getFrom().getId();
                handleListTasks(userId, message, callbackData);
            }
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleAddTask(String messageText, long userId, SendMessage message) {
        try {
            String[] parts = messageText.replace("/add ", "").split("\\|");
            if (parts.length < 1) {
                message.setText("Формат: /add <название> | <описание> | <дедлайн>");
                return;
            }
            String title = parts[0].trim();
            String description = parts.length > 1 ? parts[1].trim() : null;
            String deadline = parts.length > 2 ? parts[2].trim() : null;
            Task task = taskService.addTask(userId, title, description, deadline);
            message.setText("Задача добавлена: " + task.getTitle() + " (ID: " + task.getId() + ")");
        } catch (Exception e) {
            message.setText("Ошибка при добавлении задачи. Проверь формат дедлайна (ГГГГ-ММ-ДДTЧЧ:ММ).");
        }
    }

    private void handleListTasks(long userId, SendMessage message, String filter) {
        List<Task> tasks;
        if ("completed".equals(filter)) {
            tasks = taskService.getTasks(userId, true);
        } else if ("not_completed".equals(filter)) {
            tasks = taskService.getTasks(userId, false);
        } else {
            tasks = taskService.getTasks(userId);
        }

        if (tasks.isEmpty()) {
            message.setText("У вас нет задач.");
            return;
        }

        StringBuilder response = new StringBuilder("Ваши задачи:\n");
        for (Task task : tasks) {
            response.append(String.format("ID: %d | %s | %s | %s | %s\n",
                    task.getId(),
                    task.getTitle(),
                    task.getDescription() != null ? task.getDescription() : "Без описания",
                    task.getDeadline() != null ? task.getDeadline().toString() : "Без дедлайна",
                    task.isCompleted() ? "Выполнена" : "Не выполнена"));
        }

        // Добавляем inline-кнопки для фильтрации
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("Все", "all"));
        row.add(createButton("Выполненные", "completed"));
        row.add(createButton("Невыполненные", "not_completed"));
        keyboard.add(row);
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);

        message.setText(response.toString());
    }

    private void handleShowTasksForAction(long userId, SendMessage message, String action) {
        List<Task> tasks = taskService.getTasks(userId);
        if (tasks.isEmpty()) {
            message.setText("У вас нет задач.");
            return;
        }

        StringBuilder response = new StringBuilder("Выберите задачу для " + (action.equals("done") ? "выполнения" : "удаления") + ":\n");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Task task : tasks) {
            response.append(String.format("ID: %d | %s | %s\n",
                    task.getId(), task.getTitle(), task.isCompleted() ? "Выполнена" : "Не выполнена"));
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton("ID " + task.getId(), action + "_" + task.getId()));
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        message.setText(response.toString());
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}