package com.watertribe.todo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubTaskRequest {

    private String task;

    private String description;

    private boolean completed;
}

