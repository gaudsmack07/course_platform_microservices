package com.costacloud.searchservice.controllers;

import com.costacloud.searchservice.models.Course;
import com.costacloud.searchservice.models.Creator;
import com.costacloud.searchservice.repositories.CourseRepository;
import com.costacloud.searchservice.repositories.CreatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CreatorRepository creatorRepository;

    @GetMapping("/title/{title}")
    public List<Course> getCoursesByTitle(@PathVariable String title) {
        return courseRepository.findCourseByTitleContainingIgnoreCase(title);
    }
    @GetMapping("/keyword/{keyword}")
    public List<Course> getCoursesByKeyword(@PathVariable String keyword) {
        return courseRepository.findCourseByDescriptionContainingIgnoreCase(keyword);
    }

    @GetMapping("/creator/{creatorName}")
    public List<Course> getCoursesByCreator(@PathVariable String creatorName) {
        List<Creator> creatorsContainingName = creatorRepository.findCreatorByNameContainingIgnoreCase(creatorName);
        List<Course> result = new ArrayList<>();
        for(Creator creator : creatorsContainingName) {
            result.addAll(courseRepository.findCourseByCreatorId(creator.getId()));
        }
        return result;
    }

//    @PostMapping(path = "/temp", consumes = {"application/json"})
//    public void something(@RequestBody Course course) {
//        creatorRepository.save(course.getCreator());
//        courseRepository.save(course);
//    }
}
