package com.costacloud.userservice.controller;

import com.costacloud.userservice.models.*;
import com.costacloud.userservice.respository.CourseRepository;
import com.costacloud.userservice.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private KafkaTemplate kafkaTemplate;
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

        UserActivity userActivity = new UserActivity();
        Activity activity = new Activity();
        activity.setCourseId(courseId);
        activity.setAction(Action.ADDED);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setEntityDesc(courseOptional.get().getTitle());
        userActivity.setUsername(userName);
        userActivity.setActivity(activity);
        kafkaTemplate.send("userActivity", userActivity);

        return ResponseEntity.ok("Course added to user");
    }

    @PutMapping("/{userName}/delete/{courseId}")
    public ResponseEntity<?> removeCourse(@PathVariable String courseId, @PathVariable String userName) {
        User user = userRepository.findById(userName).get();
        user.removeCourse(courseId);
        userRepository.save(user);

        UserActivity userActivity = new UserActivity();
        Activity activity = new Activity();
        activity.setCourseId(courseId);
        activity.setAction(Action.REMOVED);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setEntityDesc(courseRepository.findById(courseId).get().getTitle());
        userActivity.setUsername(userName);
        userActivity.setActivity(activity);
        kafkaTemplate.send("userActivity", userActivity);

        return ResponseEntity.ok("Course withdrawn from");
    }
}
