package com.costacloud.useractivityservice;

import com.costacloud.useractivityservice.models.Activity;
import com.costacloud.useractivityservice.models.LoggedActivity;
import com.costacloud.useractivityservice.models.UserActivity;
import com.costacloud.useractivityservice.repositories.LoggedActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.ArrayList;
import java.util.Optional;

@SpringBootApplication
public class UserActivityServiceApplication {

	@Autowired
	private LoggedActivityRepository loggedActivityRepository;

	public static void main(String[] args) {
		SpringApplication.run(UserActivityServiceApplication.class, args);
	}

	@KafkaListener(topics = "userActivity")
	public void logActivity(UserActivity userActivity) {
		Optional<LoggedActivity> loggedActivityOptional = loggedActivityRepository.findById(userActivity.getUsername());
		LoggedActivity loggedActivity;
		if (loggedActivityOptional.isEmpty()) {
			loggedActivity = new LoggedActivity();
			loggedActivity.setActivityList(new ArrayList<Activity>());
			loggedActivity.setUsername(userActivity.getUsername());
		} else {
			loggedActivity = loggedActivityOptional.get();
		}
		loggedActivity.addActivity(userActivity.getActivity());
		loggedActivityRepository.save(loggedActivity);
	}
}
