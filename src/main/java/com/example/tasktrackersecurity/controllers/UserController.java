package com.example.tasktrackersecurity.controllers;

import com.example.tasktrackersecurity.api.v1.request.UserRq;
import com.example.tasktrackersecurity.api.v1.response.UserRs;
import com.example.tasktrackersecurity.mapper.UserMapper;
import com.example.tasktrackersecurity.publisher.UserUpdatePublisher;
import com.example.tasktrackersecurity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final UserUpdatePublisher userUpdatePublisher;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<String>> getUserInfo(Mono<Principal> principal){
        return principal.map(Principal::getName)
                .map(name -> ResponseEntity.ok("Method getUserInfo calling. Username " + name));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Flux<UserRs> getAllUsers(){
        return userService.findAllUsers().map(UserMapper.INSTANCE::toDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<UserRs>> getByIdUser(@PathVariable String id){
        return userService.findByIdUser(id).map(UserMapper.INSTANCE::toDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<UserRs>> createUser(@RequestBody UserRq userRq){
        return userService.createAccount(UserMapper.INSTANCE.toModel(userRq))
                .map(UserMapper.INSTANCE::toDTO)
                .doOnSuccess(userUpdatePublisher::publishUser)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<UserRs>> putUser(@PathVariable String id, @RequestBody UserRq userRq){
        return userService.updateUser(id, UserMapper.INSTANCE.toModel(userRq))
                .map(UserMapper.INSTANCE::toDTO)
                .doOnSuccess(userUpdatePublisher::publishUser)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id){
        return userService.deleteByIdUser(id).then(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')")
    public Flux<ServerSentEvent<UserRs>> getUserUpdate(){
        return userUpdatePublisher.getUpdatesUserSink()
                .asFlux()
                .map(userRs -> ServerSentEvent.<UserRs>builder(userRs).build());
    }
}
