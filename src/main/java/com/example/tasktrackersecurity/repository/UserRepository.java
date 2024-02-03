package com.example.tasktrackersecurity.repository;


import com.example.tasktrackersecurity.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {


    Mono<User> findByUsername(String username);

    Flux<User> findAllById(Iterable<String> id);

}
