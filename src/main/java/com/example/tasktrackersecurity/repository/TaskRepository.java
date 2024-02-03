package com.example.tasktrackersecurity.repository;


import com.example.tasktrackersecurity.entity.TaskEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TaskRepository extends ReactiveMongoRepository<TaskEntity, String> {
}
