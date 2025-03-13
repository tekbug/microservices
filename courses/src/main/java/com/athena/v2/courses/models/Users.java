package com.athena.v2.courses.models;

import com.athena.v2.libraries.enums.UserRoles;
import com.athena.v2.libraries.enums.UserStatus;
import com.athena.v2.users.models.Address;
import com.athena.v2.users.models.PhoneNumber;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    @Column(unique = true)
    private String username;

    @Email
    @Column(unique = true)
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn
    private List<Address> addressList = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn
    private List<PhoneNumber> phoneNumbers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Enumerated(EnumType.STRING)
    private UserRoles userRoles;

    private LocalDateTime lastLogin;
    private LocalDateTime lastActivity;

    private String registrationSource;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
