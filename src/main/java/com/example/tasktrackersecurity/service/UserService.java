package com.example.tasktrackersecurity.service;

import com.example.tasktrackersecurity.entity.User;
import com.example.tasktrackersecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public Mono<User> createAccount(User userRq){

        var user = new User();
        user.setPassword(passwordEncoder.encode(userRq.getPassword()));
        user.setUsername(userRq.getUsername());
        user.setEmail(userRq.getEmail());
        user.setRoles(userRq.getRoles());

        return userRepository.save(user);
    }

    public Mono<User> findByIdUser(String id){
        return userRepository.findById(id);
    }

    public Mono<User> updateUser(String id, User user){

        return findByIdUser(id).flatMap(userForUpdate -> {

            if (user.getUsername() != null) userForUpdate.setUsername(user.getUsername());
            if (user.getPassword() != null) userForUpdate.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRoles() != null && !user.getRoles().isEmpty()) userForUpdate.setRoles(user.getRoles());
            if (user.getEmail() != null) userForUpdate.setEmail(user.getEmail());

            return userRepository.save(userForUpdate);
        });
    }

    public Mono<Void> deleteByIdUser(String id){
        return userRepository.deleteById(id);
    }

    public Flux<User> findAllUsers(){
        return userRepository.findAll();
    }

}
