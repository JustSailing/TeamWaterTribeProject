package com.watertribe.todo.controller;

import com.watertribe.todo.dto.MainTodoRequest;
import com.watertribe.todo.dto.MainTodoResponse;
import com.watertribe.todo.entity.User;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.service.MainTodoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/main-todos")
@RequiredArgsConstructor
public class MainTodoController {

  private final MainTodoService mainTodoService;
  private final UserRepository userRepository;

 private Long getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = (principal instanceof UserDetails) 
                          ? ((UserDetails) principal).getUsername() 
                          : principal.toString();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
            
        return user.getId();
    }

  @PostMapping
  public MainTodoResponse createMainTodo(
      @RequestBody MainTodoRequest todorequest,
      HttpServletRequest request) {
 Long userId = getAuthenticatedUserId();
    return mainTodoService.createMainTodo(todorequest, userId);
  }

  @GetMapping
  public List<MainTodoResponse> getAllMainTodos(HttpServletRequest request) {
    Long userId = getAuthenticatedUserId();
    return mainTodoService.getAllMainTodos(userId);
  }

  @GetMapping("/{id}")
  public MainTodoResponse getMainTodoById(
      @PathVariable Long id,
      HttpServletRequest request) {
    Long userId = getAuthenticatedUserId();
    return mainTodoService.getMainTodoById(id, userId);
  }

  @PutMapping("/{id}")
  public MainTodoResponse updateMainTodo(
      @PathVariable Long id,
      @RequestBody MainTodoRequest todorequest,
      HttpServletRequest request) {
    Long userId = getAuthenticatedUserId();
    return mainTodoService.updateMainTodo(id, userId, todorequest);
  }

  @DeleteMapping("/{id}")
  public String deleteMainTodo(
      @PathVariable Long id,
      HttpServletRequest request) {
    Long userId = getAuthenticatedUserId();
    mainTodoService.deleteMainTodo(id, userId);
    return "Main todo deleted successfully";
  }
}