package com.costacloud.userservice.controller;

import com.costacloud.userservice.models.User;
import com.costacloud.userservice.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(String userId) {
        return ResponseEntity.ok(userRepository.findById(userId));
    }
}
