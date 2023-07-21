package com.costacloud.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String username;
    private String name;
    @DocumentReference
    private List<Course> enrolledCourses;

    public void addCourse(Course course) {
        enrolledCourses.add(course);
    }

    public void removeCourse(String courseId) {
        for (int i = 0; i < enrolledCourses.size(); i++) {
            if (enrolledCourses.get(i).getId().equals(courseId)) {
                enrolledCourses.remove(i);
            }
        }
    }
}
