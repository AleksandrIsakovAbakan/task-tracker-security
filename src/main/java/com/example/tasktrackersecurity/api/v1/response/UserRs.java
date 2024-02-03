package com.example.tasktrackersecurity.api.v1.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRs {

    private String id;

    private String username;

    private String password;

    private String email;

    private List<String> roles;
}
