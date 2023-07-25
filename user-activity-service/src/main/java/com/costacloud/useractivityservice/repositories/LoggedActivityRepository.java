package com.costacloud.useractivityservice.repositories;

import com.costacloud.useractivityservice.models.LoggedActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LoggedActivityRepository extends MongoRepository<LoggedActivity, String> {
}
