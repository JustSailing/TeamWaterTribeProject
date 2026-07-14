package com.watertribe.todo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubTaskResponse {

    private Long id;

    private String task;

    private String description;

    private boolean completed;

    private Long mainTodoId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
