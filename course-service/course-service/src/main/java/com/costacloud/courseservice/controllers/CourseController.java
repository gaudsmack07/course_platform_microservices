package com.costacloud.courseservice.controllers;

import com.costacloud.courseservice.models.*;
import com.costacloud.courseservice.repositories.CourseRepository;
import com.costacloud.courseservice.repositories.CreatorRepository;
import com.costacloud.courseservice.services.CourseService;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/course")
public class CourseController {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private CourseService courseService;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CreatorRepository creatorRepository;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourse(@PathVariable String id, @RequestHeader("Authorization") String token) {
        String username = courseService.getUsernameFromToken(token);
        UserActivity userActivity = new UserActivity();
        Activity activity = new Activity();
        activity.setCourseId(id);
        activity.setAction(Action.VIEWED);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setEntityDesc(courseRepository.findById(id).get().getTitle());
        userActivity.setUsername(username);
        userActivity.setActivity(activity);
        kafkaTemplate.send("userActivity", userActivity);

        return ResponseEntity.ok(courseRepository.findById(id));
    }
    @PostMapping
    public ResponseEntity<?> addCourse(@RequestBody Course course, @RequestHeader("Authorization") String token) {
        //add creator to db if not already there
        String creatorId = course.getCreator().getId();
        if (creatorRepository.findById(creatorId).isEmpty()) {
            creatorRepository.save(course.getCreator());
        } else {
            course.setCreator(creatorRepository.findById(creatorId).get());
        }
        //create bucket in minio - bucket naming convention - course name + creator last name
        String regex = "[^a-zA-Z0-9]";
        String result = course.getTitle().replaceAll(regex, "");
        result = result + course.getCreator().getName().replaceAll(regex, "");
        String finalBucketName = result.toLowerCase();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange("http://CONTENT-SERVICE/content/create/" + finalBucketName, HttpMethod.GET, entity, Void.class);
        course.setBucketAllotted(finalBucketName);
        //initializing files list
        course.setFilesList(new ArrayList<>());

        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PostMapping(path = "/{courseId}/content", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> addContentToCourse(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String courseId, @RequestHeader("Authorization") String token) throws IOException {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        if (!course.getFilesList().contains(file.getOriginalFilename())){
            course.addFile(file.getOriginalFilename());
        }
        courseRepository.save(course);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", token);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // Send the multipart file to the target URL
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return ResponseEntity.ok(responseEntity.getBody());
    }

    @GetMapping("/{courseId}/content/{fileName}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String courseId, @PathVariable String fileName, @RequestHeader("Authorization") String token) {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        String username = courseService.getUsernameFromToken(token);
        UserActivity userActivity = new UserActivity();
        Activity activity = new Activity();
        activity.setCourseId(courseId);
        activity.setAction(Action.DOWNLOADED);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setEntityDesc(fileName);
        userActivity.setUsername(username);
        userActivity.setActivity(activity);
        kafkaTemplate.send("userActivity", userActivity);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files/" + fileName, HttpMethod.GET,entity, ByteArrayResource.class);
    }

    @GetMapping("/{courseId}/content")
    public ResponseEntity<?> getFileList(@PathVariable String courseId, @RequestHeader("Authorization") String token) {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        String username = courseService.getUsernameFromToken(token);
        UserActivity userActivity = new UserActivity();
        Activity activity = new Activity();
        activity.setCourseId(courseId);
        activity.setAction(Action.VIEWED);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setEntityDesc("List of all contents");
        userActivity.setUsername(username);
        userActivity.setActivity(activity);
        kafkaTemplate.send("userActivity", userActivity);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files", HttpMethod.GET, entity, List.class);
    }

    @PutMapping("/{courseId}")
    public void modifyCourseDesc(@RequestBody Course course, @PathVariable String courseId) {
        Course savedCourse = courseRepository.findById(courseId).get();
        if (course.getDescription() != null) {
            savedCourse.setDescription(course.getDescription());
        }
        courseRepository.save(savedCourse);
    }

    @DeleteMapping("/{courseId}")
    public void deleteCourse(@PathVariable String courseId,@RequestHeader("Authorization") String token) {
        if (courseRepository.findById(courseId).isEmpty()) {
            return;
        }
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName, HttpMethod.DELETE, entity, Void.class);
        courseRepository.deleteById(courseId);
    }

    @DeleteMapping("/{courseId}/{fileName}")
    public void deleteContent(@PathVariable String courseId, @PathVariable String fileName,@RequestHeader("Authorization") String token) {
        Course course = courseRepository.findById(courseId).get();
        course.getFilesList().remove(fileName);
        String bucketName = course.getBucketAllotted();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files/" + fileName, HttpMethod.DELETE, entity, Void.class);
        courseRepository.save(course);
    }

}
