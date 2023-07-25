package com.costacloud.courseservice.services;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

    public String getUsernameFromToken(String token) {
        String payload = token.split("\\.")[1];
        String decodedPayload = new String(Base64.decodeBase64(payload));
        String username = decodedPayload.split("preferred_username")[1].split("\"")[2];
        return username;
    }
}
