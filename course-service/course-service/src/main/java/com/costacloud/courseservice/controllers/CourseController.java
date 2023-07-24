package com.costacloud.courseservice.controllers;

import com.costacloud.courseservice.models.Course;
import com.costacloud.courseservice.models.Creator;
import com.costacloud.courseservice.repositories.CourseRepository;
import com.costacloud.courseservice.repositories.CreatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        restTemplate.getForObject("http://CONTENT-SERVICE/content/create/" + finalBucketName, Void.class);
        course.setBucketAllotted(finalBucketName);
        //initializing files list
        course.setFilesList(new ArrayList<>());

        return ResponseEntity.ok(courseRepository.save(course));
    }

    @PostMapping(path = "/{courseId}/content", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> addContentToCourse(@RequestPart(value="file", required=false) MultipartFile file, @PathVariable String courseId) throws IOException {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        if (!course.getFilesList().contains(file.getOriginalFilename())){
            course.addFile(file.getOriginalFilename());
        }
        courseRepository.save(course);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        // Send the multipart file to the target URL
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        return ResponseEntity.ok(responseEntity.getBody());
    }

    @GetMapping("/{courseId}/content/{fileName}")
    public ByteArrayResource downloadFile(@PathVariable String courseId, @PathVariable String fileName) {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        return restTemplate.getForObject("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files/" + fileName, ByteArrayResource.class);
    }

    @GetMapping("/{courseId}/content")
    public List<String> getFileList(@PathVariable String courseId) {
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();

        return restTemplate.getForObject("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files", List.class);
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
    public void deleteCourse(@PathVariable String courseId) {
        if (courseRepository.findById(courseId).isEmpty()) {
            return;
        }
        Course course = courseRepository.findById(courseId).get();
        String bucketName = course.getBucketAllotted();
        restTemplate.delete("http://CONTENT-SERVICE/content/bucket/" + bucketName);
        courseRepository.deleteById(courseId);
    }

    @DeleteMapping("/{courseId}/{fileName}")
    public void deleteContent(@PathVariable String courseId, @PathVariable String fileName) {
        Course course = courseRepository.findById(courseId).get();
        course.getFilesList().remove(fileName);
        String bucketName = course.getBucketAllotted();
        restTemplate.delete("http://CONTENT-SERVICE/content/bucket/" + bucketName + "/files/" + fileName);
        courseRepository.save(course);
    }

}
