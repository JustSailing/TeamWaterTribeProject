package com.watertribe.todo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "main_todos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainTodo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String task;

    private String description;

    private boolean completed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Builder.Default
    @OneToMany(mappedBy = "mainTodo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTask> subTasks = new ArrayList<>();

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        completed = false;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
        subTask.setMainTodo(this);
    }

    public void removeSubTask(Long subTaskId) {
        subTasks.stream()
                .filter(task -> task.getId().equals(subTaskId))
                .findFirst()
                .ifPresent(task -> {
                    task.setMainTodo(null); 
                    subTasks.remove(task);
                });
    }
}