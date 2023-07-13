package com.costacloud.searchservice.controllers;

import com.costacloud.searchservice.models.Course;
import com.costacloud.searchservice.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping("/title/{title}")
    public List<Course> getCoursesByTitle(@PathVariable String title) {
        return courseRepository.findCourseByTitleContainingIgnoreCase(title);
    }
    @GetMapping("/{keyword}")
    public List<Course> getCoursesByKeyword(@PathVariable String keyword) {
        return courseRepository.findCourseByDescriptionContainingIgnoreCase(keyword);
    }
}
