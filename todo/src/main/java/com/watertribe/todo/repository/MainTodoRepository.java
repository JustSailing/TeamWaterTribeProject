package com.watertribe.todo.repository;

import com.watertribe.todo.entity.MainTodo;
import com.watertribe.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MainTodoRepository extends JpaRepository<MainTodo, Long> {

    List<MainTodo> findByUser(User user);

    Optional<MainTodo> findByIdAndUser(Long id, User user);
}