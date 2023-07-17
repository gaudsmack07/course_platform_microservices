package com.costacloud.courseservice.repositories;

import com.costacloud.courseservice.models.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findCourseByTitleContainingIgnoreCase(String tag);

    List<Course> findCourseByDescriptionContainingIgnoreCase(String keyword);

    List<Course> findCourseByCreatorId(String id);

}
