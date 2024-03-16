package com.gritlab.buy01.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gritlab.buy01.userservice.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Field
    @NotNull
    @Size(min=3, max=40, message="Error: Name has to be between 3 and 40 characters long")
    private String name;

    @Field
    @NotNull
    @Email
    @Size(min = 0, max = 320, message = "Error: Invalid email length")
    private String email;

    @Field
    @NotNull
    @JsonIgnore
    private String password;

    @Field
    @NotNull
    private Role role;

    @Field
    private String avatar;

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
