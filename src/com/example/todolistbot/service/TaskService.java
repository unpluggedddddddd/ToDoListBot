package com.example.todolistbot.service;

import com.example.todolistbot.entity.Task;
import com.example.todolistbot.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task addTask(Long userId, String title, String description, String deadline) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Название задачи не может быть пустым");
        }
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : null);
        if (deadline != null && !deadline.isEmpty()) {
            try {
                task.setDeadline(LocalDateTime.parse(deadline, FORMATTER));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Неверный формат дедлайна. Используйте ДД.ММ.ГГГГ ЧЧ:ММ");
            }
        }
        return taskRepository.save(task);
    }

    public List<Task> getTasks(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getTasks(Long userId, boolean completed) {
        return taskRepository.findByUserIdAndCompleted(userId, completed);
    }

    public void markTaskAsCompleted(Long taskId, Long userId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (!task.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Вы не можете отметить чужую задачу");
            }
            task.setCompleted(true);
            taskRepository.save(task);
        });
    }

    public void deleteTask(Long taskId, Long userId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (!task.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Вы не можете удалить чужую задачу");
            }
            taskRepository.deleteById(taskId);
        });
    }
    public DateTimeFormatter getFormatter() {
        return FORMATTER;
    }
    public List<Task> findTasksWithUpcomingDeadline(LocalDateTime now, LocalDateTime threshold) {
        return taskRepository.findAll().stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isAfter(now) && task.getDeadline().isBefore(threshold))
                .toList();
    }
}
