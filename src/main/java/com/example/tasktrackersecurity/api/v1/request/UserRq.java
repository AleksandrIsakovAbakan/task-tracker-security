package com.example.tasktrackersecurity.api.v1.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRq {

    private String id;

    private String username;

    private String password;

    private String email;

    private List<String> roles;
}
