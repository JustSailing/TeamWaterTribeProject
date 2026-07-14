package com.watertribe.todo.controller;

import com.watertribe.todo.dto.SubTaskRequest;
import com.watertribe.todo.dto.SubTaskResponse;
import com.watertribe.todo.service.SubTaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-todos/{mainTodoId}/subtasks")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;

    @PostMapping
    public SubTaskResponse createSubTask(
            @PathVariable Long mainTodoId,
            @RequestBody SubTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("userId"));
        return subTaskService.createSubTask(mainTodoId, request, userId);
    }

    @GetMapping
    public List<SubTaskResponse> getAllSubTasks(
            @PathVariable Long mainTodoId,
            HttpServletRequest httpRequest
    ) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("userId"));
        return subTaskService.getAllSubTasks(mainTodoId, userId);
    }

    @GetMapping("/{id}")
    public SubTaskResponse getSubTaskById(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("userId"));
        return subTaskService.getSubTaskById(mainTodoId, id, userId);
    }

    @PutMapping("/{id}")
    public SubTaskResponse updateSubTask(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            @RequestBody SubTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("userId"));
        return subTaskService.updateSubTask(mainTodoId, id, request, userId);
    }

    @DeleteMapping("/{id}")
    public String deleteSubTask(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = Long.valueOf((String) httpRequest.getAttribute("userId"));
        subTaskService.deleteSubTask(mainTodoId, id, userId);
        return "Sub task deleted successfully";
    }
}
