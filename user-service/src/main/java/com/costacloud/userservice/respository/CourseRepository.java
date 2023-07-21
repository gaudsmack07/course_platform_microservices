package com.costacloud.userservice.respository;

import com.costacloud.userservice.models.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CourseRepository extends MongoRepository<Course, String> {
}
