package com.budgettracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String name;
    private String lastname;
    private String username;
    private String password;
}