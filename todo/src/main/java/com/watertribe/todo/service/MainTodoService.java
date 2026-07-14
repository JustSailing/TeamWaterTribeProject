package com.watertribe.todo.service;

import com.watertribe.todo.dto.MainTodoRequest;
import com.watertribe.todo.dto.MainTodoResponse;
import com.watertribe.todo.entity.MainTodo;
import com.watertribe.todo.entity.User;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MainTodoService {

    private final MainTodoRepository mainTodoRepository;
    private final UserRepository userRepository;

    public MainTodoResponse createMainTodo(
            MainTodoRequest request,
            Long userId
    ) {
        User user = getLoggedInUser(userId);

        MainTodo mainTodo = MainTodo.builder()
                .task(request.getTask())
                .description(request.getDescription())
                .completed(false)
                .user(user)
                .build();

        MainTodo savedTodo = mainTodoRepository.saveAndFlush(mainTodo);

        return mapToResponse(savedTodo);
    }

    public List<MainTodoResponse> getAllMainTodos(Long userId) {
        User user = getLoggedInUser(userId);

        return mainTodoRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MainTodoResponse getMainTodoById(
            Long id,
            Long userId
    ) {
        User user = getLoggedInUser(userId);

        MainTodo mainTodo = mainTodoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Main todo not found"));

        return mapToResponse(mainTodo);
    }

    public MainTodoResponse updateMainTodo(
            Long id,
            Long userId,
            MainTodoRequest request
    ) {
        User user = getLoggedInUser(userId);

        MainTodo mainTodo = mainTodoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Main todo not found"));

        mainTodo.setTask(request.getTask());
        mainTodo.setDescription(request.getDescription());
        mainTodo.setCompleted(request.isCompleted());

        MainTodo updatedTodo = mainTodoRepository.saveAndFlush(mainTodo);

        return mapToResponse(updatedTodo);
    }

    public void deleteMainTodo(
            Long id,
            Long userId
    ) {
        User user = getLoggedInUser(userId);

        MainTodo mainTodo = mainTodoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Main todo not found"));

        mainTodoRepository.delete(mainTodo);
        mainTodoRepository.flush();
    }

    private User getLoggedInUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private MainTodoResponse mapToResponse(MainTodo mainTodo) {
        return MainTodoResponse.builder()
                .id(mainTodo.getId())
                .task(mainTodo.getTask())
                .description(mainTodo.getDescription())
                .completed(mainTodo.isCompleted())
                .createdAt(mainTodo.getCreatedAt())
                .updatedAt(mainTodo.getUpdatedAt())
                .build();
    }
}