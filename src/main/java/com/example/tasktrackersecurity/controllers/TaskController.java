package com.example.tasktrackersecurity.controllers;

import com.example.tasktrackersecurity.api.v1.request.TaskRq;
import com.example.tasktrackersecurity.api.v1.response.TaskRs;
import com.example.tasktrackersecurity.entity.TaskEntity;
import com.example.tasktrackersecurity.mapper.TaskEntityMapper;
import com.example.tasktrackersecurity.publisher.TaskUpdatePublisher;
import com.example.tasktrackersecurity.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    private final TaskUpdatePublisher taskUpdatePublisher;


    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Flux<TaskRs> getAllTasks(){
        return taskService.findAllTasks().map(TaskEntityMapper.INSTANCE::toDTO).cache();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<TaskRs>> getByIdTask(@PathVariable String id){
        Mono<TaskEntity> byIdTask = taskService.getByIdTask(id);
        return taskService.getByTaskEntityTwo(byIdTask)
                .map(TaskEntityMapper.INSTANCE::toDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build()).cache();
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public Mono<ResponseEntity<TaskRs>> createTask(@RequestBody TaskRq taskRq){
        return taskService.saveTask(TaskEntityMapper.INSTANCE.toModel(taskRq))
                .map(TaskEntityMapper.INSTANCE::toDTO)
                .doOnSuccess(taskUpdatePublisher::publishTask)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Mono<ResponseEntity<TaskRs>> putTask(@PathVariable String id, @RequestBody TaskRq taskRq){
        return taskService.updateTask(id, TaskEntityMapper.INSTANCE.toModel(taskRq))
                .map(TaskEntityMapper.INSTANCE::toDTO)
                .doOnSuccess(taskUpdatePublisher::publishTask)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/observersAdd/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<TaskRs>> getTaskObservers(@PathVariable String id, @RequestParam String idObservers){
        return taskService.updateTaskObservers(id, idObservers)
                .map(TaskEntityMapper.INSTANCE::toDTO)
                .doOnSuccess(taskUpdatePublisher::publishTask)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id){
        return taskService.deleteByIdTask(id).then(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Flux<ServerSentEvent<TaskRs>> getTaskUpdate(){
        return taskUpdatePublisher.getUpdatesTaskSink()
                .asFlux()
                .map(taskRs -> ServerSentEvent.<TaskRs>builder(taskRs).build());
    }
}
