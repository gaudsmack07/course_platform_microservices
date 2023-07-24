package com.costacloud.userservice.controller;

import com.costacloud.userservice.models.Course;
import com.costacloud.userservice.models.User;
import com.costacloud.userservice.respository.CourseRepository;
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
    @Autowired
    private CourseRepository courseRepository;
    @GetMapping("/{userName}")
    public ResponseEntity<?> getUser(@PathVariable String userName) {
        return ResponseEntity.ok(userRepository.findById(userName));
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
    @PutMapping("/{userName}/add/{courseId}")
    public ResponseEntity<?> enrolCourse(@PathVariable String courseId, @PathVariable String userName) {
        User user = userRepository.findById(userName).get();
        var courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isPresent()) {
            user.addCourse(courseOptional.get());
            userRepository.save(user);
        } else {
            throw new RuntimeException("No such course");
        }
        return ResponseEntity.ok("Course added to user");
    }

    @PutMapping("/{userName}/delete/{courseId}")
    public ResponseEntity<?> removeCourse(@PathVariable String courseId, @PathVariable String userName) {
        User user = userRepository.findById(userName).get();
        user.removeCourse(courseId);
        userRepository.save(user);
        return ResponseEntity.ok("Course withdrawn from");
    }
}
