package com.watertribe.todo.controller;

import com.watertribe.todo.dto.MainTodoRequest;
import com.watertribe.todo.dto.MainTodoResponse;
import com.watertribe.todo.service.MainTodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/main-todos")
@RequiredArgsConstructor
public class MainTodoController {

    private final MainTodoService mainTodoService;

    @PostMapping
    public MainTodoResponse createMainTodo(
            @RequestBody MainTodoRequest todorequest,
            HttpServletRequest request
    ) {
        Long UserId = Long.valueOf((String) request.getAttribute("userId"));
        return mainTodoService.createMainTodo(todorequest, UserId);
    }

    @GetMapping
    public List<MainTodoResponse> getAllMainTodos(HttpServletRequest request) {
        Long UserId = Long.valueOf((String) request.getAttribute("userId"));
        return mainTodoService.getAllMainTodos(UserId);
    }

    @GetMapping("/{id}")
    public MainTodoResponse getMainTodoById(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long UserId = Long.valueOf((String) request.getAttribute("userId"));
        return mainTodoService.getMainTodoById(id, UserId);
    }

    @PutMapping("/{id}")
    public MainTodoResponse updateMainTodo(
            @PathVariable Long id,
            @RequestBody MainTodoRequest todorequest,
            HttpServletRequest request
    ) {
        Long UserId = Long.valueOf((String) request.getAttribute("userId"));
        return mainTodoService.updateMainTodo(id, UserId, todorequest);
    }

    @DeleteMapping("/{id}")
    public String deleteMainTodo(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long UserId = Long.valueOf((String) request.getAttribute("userId"));
        mainTodoService.deleteMainTodo(id, UserId);
        return "Main todo deleted successfully";
    }
}