package com.costacloud.searchservice.repositories;

import com.costacloud.searchservice.models.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findCourseByTitleContainingIgnoreCase(String tag);

    List<Course> findCourseByDescriptionContainingIgnoreCase(String keyword);
}
