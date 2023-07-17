package com.costacloud.courseservice.repositories;

import com.costacloud.courseservice.models.Creator;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CreatorRepository extends MongoRepository<Creator, String> {
    List<Creator> findCreatorByNameContainingIgnoreCase(String name);
}
