package com.athena.v2.gateway.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class UserController {

    @GetMapping("tests/users")
    public Mono<String> getUsers() {
        return Mono.just("Testing works");
    }
}