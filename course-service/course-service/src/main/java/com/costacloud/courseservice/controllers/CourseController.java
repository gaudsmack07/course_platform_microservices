package com.costacloud.courseservice.controllers;

import com.costacloud.courseservice.models.Course;
import com.costacloud.courseservice.models.Creator;
import com.costacloud.courseservice.repositories.CourseRepository;
import com.costacloud.courseservice.repositories.CreatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CreatorRepository creatorRepository;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@RequestBody String id) {
        return ResponseEntity.ok(courseRepository.findById(id));
    }
    @PostMapping("")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        //add creator to db if not already there
        Creator creator = course.getCreator();
        String creatorName = creator.getName();
        if (creatorRepository.findCreatorByNameContainingIgnoreCase(creatorName).isEmpty()) {
            creatorRepository.save(creator);
        }
        //create bucket in minio - bucket naming convention - course name + creator last name
        String regex = "[^a-zA-Z0-9]";
        String result = course.getTitle().replaceAll(regex, "");
        result = result + course.getCreator().getName().replaceAll(regex, "");
        String finalBucketName = result.toLowerCase();
        restTemplate.getForObject("http://CONTENT-SERVICE/content/create/" + finalBucketName, Void.class);

        return ResponseEntity.ok(courseRepository.save(course));
    }

}
