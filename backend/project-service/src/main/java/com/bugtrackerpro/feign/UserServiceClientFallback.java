package com.bugtrackerpro.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component 
public class UserServiceClientFallback implements UserServiceClient{
    
    private static final Logger log =
            LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public UserResponse getUserById(Long id) {

        log.warn(
                "User Service is unavailable. " +
                "Unable to retrieve user information for userId={}",
                id
        );

        throw new RuntimeException(
                "User Service is temporarily unavailable. " +
                "Unable to retrieve user information for userId=" + id
        );
    }
}
