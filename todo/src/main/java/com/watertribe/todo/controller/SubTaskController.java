package com.watertribe.todo.controller;

import com.watertribe.todo.dto.SubTaskRequest;
import com.watertribe.todo.dto.SubTaskResponse;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.service.SubTaskService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/main-todos/{mainTodoId}/subtasks")
@RequiredArgsConstructor
public class SubTaskController {

    private final SubTaskService subTaskService;
    private final UserRepository userRepository;

    private Long getAuthenticatedUserId() {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails) 
                          ? ((UserDetails) principal).getUsername() 
                          : principal.toString();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
    }

    @PostMapping
    public SubTaskResponse createSubTask(
            @PathVariable Long mainTodoId,
            @RequestBody SubTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        return subTaskService.createSubTask(mainTodoId, request, getAuthenticatedUserId());
    }

    @GetMapping
    public List<SubTaskResponse> getAllSubTasks(
            @PathVariable Long mainTodoId,
            HttpServletRequest httpRequest
    ) {
        return subTaskService.getAllSubTasks(mainTodoId, getAuthenticatedUserId());
    }

    @GetMapping("/{id}")
    public SubTaskResponse getSubTaskById(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        return subTaskService.getSubTaskById(mainTodoId, id, getAuthenticatedUserId());
    }

    @PutMapping("/{id}")
    public SubTaskResponse updateSubTask(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            @RequestBody SubTaskRequest request,
            HttpServletRequest httpRequest
    ) {
        return subTaskService.updateSubTask(mainTodoId, id, request, getAuthenticatedUserId());
    }

    @DeleteMapping("/{id}")
    public String deleteSubTask(
            @PathVariable Long mainTodoId,
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        subTaskService.deleteSubTask(mainTodoId, id, getAuthenticatedUserId());
        return "Sub task deleted successfully";
    }
}
