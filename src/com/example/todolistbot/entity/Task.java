package com.example.todolistbot.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDateTime deadline;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private Long userId; // ID Telegram-пользователя
}