package com.example.tasktrackersecurity.service;

import com.example.tasktrackersecurity.entity.TaskEntity;
import com.example.tasktrackersecurity.entity.User;
import com.example.tasktrackersecurity.repository.TaskRepository;
import com.example.tasktrackersecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    public Flux<TaskEntity> findAllTasks(){
        Flux<Mono<TaskEntity>> map = taskRepository.findAll().map(this::getByTaskEntity);
        return map.flatMap(Mono::flux).log();
    }

    public Mono<TaskEntity> getByTaskEntity(TaskEntity taskEntity){
        Mono<TaskEntity> taskEntityMono = Mono.just(taskEntity);
        return getByTaskEntityTwo(taskEntityMono);
    }

    public Mono<TaskEntity> getByTaskEntityTwo(Mono<TaskEntity> taskEntityMono){
        Mono<User> author = userRepository.findById(taskEntityMono.map(TaskEntity::getAuthorId));
        Mono<User> assignee = userRepository.findById(taskEntityMono.map(TaskEntity::getAssigneeId));
        Mono<Set<User>> observers = userRepository.findAllById(taskEntityMono.map(TaskEntity::getObserverIds)
                        .flatMapIterable(strings -> strings.stream()
                                .toList()))
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .collect(Collectors.toSet());

        return Mono.zip(taskEntityMono, author, assignee, observers).map(t -> new TaskEntity(
                t.getT1().getId(),
                t.getT1().getName(),
                t.getT1().getDescription(),
                t.getT1().getCreatedAt(),
                t.getT1().getUpdatedAt(),
                t.getT1().getStatus(),
                t.getT1().getAuthorId(),
                t.getT1().getAssigneeId(),
                t.getT1().getObserverIds(),
                t.getT2(),
                t.getT3(),
                t.getT4()));
    }

    public Mono<TaskEntity> saveTask(TaskEntity task){
        if (task.getObserverIds() != null) {
            task.setId(UUID.randomUUID().toString());
            task.setCreatedAt(Instant.now());
            return taskRepository.save(task);
        } else {
            return null;
        }
    }

    public Mono<TaskEntity> updateTask(String id, TaskEntity task) {
        if (!id.equals(task.getId())) task.setId(id);
        return getByTaskEntity(task).flatMap(taskForUpdate -> {

            taskForUpdate.setUpdatedAt(Instant.now());
            if (task.getName() != null) taskForUpdate.setName(task.getName());
            if (task.getDescription() != null) taskForUpdate.setDescription(task.getDescription());
            if (task.getCreatedAt() != null) taskForUpdate.setCreatedAt(task.getCreatedAt());
            if (task.getDescription() != null) taskForUpdate.setDescription(task.getDescription());
            if (task.getUpdatedAt() != null) taskForUpdate.setUpdatedAt(task.getUpdatedAt());
            if (task.getStatus() != null) taskForUpdate.setStatus(task.getStatus());
            if (task.getAuthorId() != null) taskForUpdate.setAuthorId(task.getAuthorId());
            if (task.getAssigneeId() != null) taskForUpdate.setAssigneeId(task.getAssigneeId());
            if (task.getObserverIds() != null) taskForUpdate.setObserverIds(task.getObserverIds());

            return taskRepository.save(taskForUpdate);
        });
    }

    public Mono<TaskEntity> updateTaskObservers(String id, String idObservers) {
        Mono<TaskEntity> taskEntityMono = taskRepository.findById(id);
        Mono<Set<String>> observersIds = taskEntityMono.map(TaskEntity::getObserverIds)
                .doOnNext(strings -> strings.add(idObservers));
        observersIds.flatMapIterable(strings -> strings.stream()
                .toList())
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .collect(Collectors.toSet());

        return Mono.zip(taskEntityMono, observersIds).flatMap(t -> {
            TaskEntity saved = new TaskEntity(
                    t.getT1().getId(),
                    t.getT1().getName(),
                    t.getT1().getDescription(),
                    t.getT1().getCreatedAt(),
                    t.getT1().getUpdatedAt(),
                    t.getT1().getStatus(),
                    t.getT1().getAuthorId(),
                    t.getT1().getAssigneeId(),
                    t.getT2(),
                    t.getT1().getAuthor(),
                    t.getT1().getAssignee(),
                    t.getT1().getObservers());
            return taskRepository.save(saved);
        });
    }

    public Mono<Void> deleteByIdTask(String id){
        return taskRepository.deleteById(id);
    }

    public Mono<TaskEntity> getByIdTask(String id) {
        return taskRepository.findById(id);
    }
}
