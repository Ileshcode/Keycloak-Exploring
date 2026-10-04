package ru.ilesh.resourceserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_table")
public class UserEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "username", unique = true, nullable = false)
    private String username;


}
