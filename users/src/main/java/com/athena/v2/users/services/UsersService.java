package com.athena.v2.users.services;

import com.athena.v2.libraries.dtos.requests.UserIdRequestDTO;
import com.athena.v2.libraries.dtos.requests.UserRequestDTO;
import com.athena.v2.libraries.dtos.responses.UserIdResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.libraries.enums.UserRoles;
import com.athena.v2.libraries.enums.UserStatus;
import com.athena.v2.users.annotations.CurrentUser;
import com.athena.v2.users.exceptions.InvalidUserStatusException;
import com.athena.v2.users.exceptions.UserAlreadyExistException;
import com.athena.v2.users.exceptions.UserNotFoundException;
import com.athena.v2.users.models.Events;
import com.athena.v2.users.models.Users;
import com.athena.v2.users.repositories.EventsRepository;
import com.athena.v2.users.repositories.UsersRepository;
import com.athena.v2.users.utils.ObjectMappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;
    private final KeycloakService keycloakService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void registerUser(UserRequestDTO requestDTO) {
        try {
            if (!validateUser(requestDTO)) {
                Users target = objectMappers.mapUsersToDatabase(requestDTO);
                Users savedUser = usersRepository.saveAndFlush(target);
                UserResponseDTO user = getUserById(savedUser.getUserId());
                log.info("Registered user in database: {}", user);
                registerUserToKeycloak(user);

                Events register = createEventForPublication(savedUser);

                rabbitTemplate.convertAndSend("user-exchange", "user.created", register);

                log.info("Published user.created event for user ID: {}", savedUser.getUserId());
            } else {
                throw new UserAlreadyExistException("User with email or username already exists.");
            }
        } catch (Exception e) {
            log.error("Registering user failed. Check the logs for more information.", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updateUser(String userId, UserRequestDTO requestDTO) {
        Users target = getUserByUserIdOrThrow(userId);

        if (!target.getUserRoles().equals(requestDTO.userRoles())) {
            throw new IllegalArgumentException("User roles cannot be updated through this endpoint.");
        }

        target.getPhoneNumbers().clear();
        target.getAddressList().clear();

        target.setUsername(requestDTO.username());
        target.setFirstName(requestDTO.firstName());
        target.setMiddleName(requestDTO.middleName());
        target.setLastName(requestDTO.lastName());
        target.setEmail(requestDTO.email());
        target.getPhoneNumbers().addAll(objectMappers.mapPhoneNumbersToDatabase(requestDTO.phoneNumbers()));
        target.getAddressList().addAll(objectMappers.mapAddressesToDatabase(requestDTO.addresses()));
        target.setUserStatus(requestDTO.userStatus());
        usersRepository.saveAndFlush(target);
        log.info("Updated user body: {}", target);

        createEventForPublication(target);
        Events event = createEventForPublication(target);

        rabbitTemplate.convertAndSend("user-exchange", "user.updated", event);

        log.info("Published user.updated event for user ID: {}", target.getUserId());
    }

    @Transactional
    protected void registerUserToKeycloak(UserResponseDTO userDTO) {
        keycloakService.registerUser(userDTO);
    }

    public UserResponseDTO getUserById(String userId) {
        return Optional.of(usersRepository.findUsersByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("USER IS NOT FOUND")))
                .map(objectMappers::mapUserFromDatabase)
                .orElse(null);
    }

    public void deleteUserById(String userId) {
        Users user = getUserByUserIdOrThrow(userId);
        user.setUserStatus(UserStatus.SUSPENDED);
        usersRepository.saveAndFlush(user);
        log.info("User {} logically deleted (status SUSPENDED)", userId);

        Events deleteEvent = createEventForPublication(user);

        rabbitTemplate.convertAndSend("user-exchange", "user.deleted", deleteEvent);

        log.info("Published user.deleted event for user ID: {}", user.getUserId());
    }

    public void updateUserStatus(String userId, String status) {
        Users user = getUserByUserIdOrThrow(userId);
        try {
            user.setUserStatus(UserStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new InvalidUserStatusException("Invalid user status provided: " + status);
        }
        usersRepository.saveAndFlush(user);
        log.info("User {} status updated to {}", userId, status);
        Events updateEvent = createEventForPublication(user);
        rabbitTemplate.convertAndSend("user-exchange", "user.updated", updateEvent);
        log.info("Published user.updated for user ID: {}", user.getUserId());
    }

    public void blockUser(String userId, String status) {
        updateUserStatus(userId, status);
    }

    public UserIdResponseDTO returnCurrentUserInformation(@CurrentUser UserIdRequestDTO userIdRequestDTO) {
        return UserIdResponseDTO.builder()
                .username(userIdRequestDTO.username())
                .email(userIdRequestDTO.email())
                .roles(userIdRequestDTO.roles())
                .isSuspended(isUserSuspended(userIdRequestDTO.username()))
                .build();
    }

    public List<UserResponseDTO> returnAllUsers() {
        List<Users> users = usersRepository.findAll();
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(objectMappers::mapUserFromDatabase)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> returnAllUsersByRole(String role) {
        List<Users> users = usersRepository.findAllByUserRoles(UserRoles.valueOf(role));
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(objectMappers::mapUserFromDatabase)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> returnUserByUserStatus(String statusCode) {
        UserStatus userStatus;
        try {
            userStatus = UserStatus.valueOf(statusCode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidUserStatusException("Invalid user status provided: " + statusCode);
        }
        List<Users> usersWithStatus = usersRepository.getUsersByUserStatus(userStatus);
        if (usersWithStatus.isEmpty()) {
            return Collections.emptyList();
        }
        return usersWithStatus.stream()
                .map(objectMappers::mapUserFromDatabase)
                .collect(Collectors.toList());
    }


    private boolean isUserSuspended(String userId) {
        return getUserByUserIdOrThrow(userId).getUserStatus() == UserStatus.SUSPENDED;
    }

    private boolean validateUser(UserRequestDTO requestDTO) {
        return usersRepository.existsByEmailOrUsername(requestDTO.email(), requestDTO.username());
    }

    private Users getUserByUserIdOrThrow(String userId) {
        return usersRepository.findUsersByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("USER WITH USER ID " + userId + " NOT FOUND"));
    }

    public Events createEventForPublication(Users target) {
        Events event = new Events();
        event.setEventId(target.getUserId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setEventType("user-exchange-events");
        event.setEntityId(target.getUserId());
        eventsRepository.saveAndFlush(event);
        return event;
    }
}
