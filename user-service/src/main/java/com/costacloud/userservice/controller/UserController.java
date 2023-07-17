package com.costacloud.userservice.controller;

import com.costacloud.userservice.models.User;
import com.costacloud.userservice.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(String userId) {
        return ResponseEntity.ok(userRepository.findById(userId));
    }
    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user) {
        user.setEnrolledCourses(new ArrayList<>());
        //username uniqueness constraint
        if (!userRepository.findByUsername(user.getUsername()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already taken");
        }
        return ResponseEntity.ok(userRepository.save(user));
    }
}
