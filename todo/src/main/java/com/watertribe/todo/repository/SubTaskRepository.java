package com.watertribe.todo.repository;

import com.watertribe.todo.entity.SubTask;
import com.watertribe.todo.entity.MainTodo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {

    List<SubTask> findByMainTodo(MainTodo mainTodo);

    Optional<SubTask> findByIdAndMainTodo(Long id, MainTodo mainTodo);

    @Modifying(clearAutomatically = true)
    @Query("delete from MainTodo m where m.id = :id")
    void deleteById(Long id);
}