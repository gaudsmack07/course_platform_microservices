package com.costacloud.courseservice.controllers;

import com.costacloud.courseservice.models.Course;
import com.costacloud.courseservice.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private CourseRepository courseRepository;
    @PostMapping("/add")
    public ResponseEntity<?> addCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PostMapping(path = "/{courseId}/files/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void uploadFile(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String bucketName) throws IOException

}
