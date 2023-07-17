package com.costacloud.courseservice.controllers;

import com.costacloud.courseservice.models.Course;
import com.costacloud.courseservice.models.Creator;
import com.costacloud.courseservice.repositories.CourseRepository;
import com.costacloud.courseservice.repositories.CreatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        String creatorName = course.getCreator().getName();
        if (creatorRepository.findCreatorByNameContainingIgnoreCase(creatorName).isEmpty()) {
            creatorRepository.save(course.getCreator());
        }
        //create bucket in minio - bucket naming convention - course name + creator last name
        String regex = "[^a-zA-Z0-9]";
        String result = course.getTitle().replaceAll(regex, "");
        result = result + course.getCreator().getName().replaceAll(regex, "");
        String finalBucketName = result.toLowerCase();
        restTemplate.getForObject("http://CONTENT-SERVICE/content/create/" + finalBucketName, Void.class);

        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PostMapping(path = "/{courseId}/content", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> addContentToCourse(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String courseId) throws IOException {
        Course course = courseRepository.findById(courseId).get();
        String courseName = course.getTitle();
        String creatorName = creatorRepository.findById(course.getCreator().getId()).get().getName();
        String regex = "[^a-zA-Z0-9]";
        String result = courseName.replaceAll(regex, "");
        result = result + creatorName.replaceAll(regex, "");
        String bucketName = result.toLowerCase();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // Send the multipart file to the target URL
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return ResponseEntity.ok(responseEntity.getBody());

    }

}
