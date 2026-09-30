package ru.ilesh.resourceserver.data;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileData {
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private List<String> roles;
}
