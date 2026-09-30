package com.bugtrackerpro.feign;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class UserServiceClientFallback implements UserServiceClient{
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
