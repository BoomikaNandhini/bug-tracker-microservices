package com.bugtrackerpro.feign;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class ProjectServiceClientFallback implements ProjectServiceClient {

    @Override
    public ProjectResponse getProjectById(Long id) {
        log.warn(
                "Project Service is unavailable. " +
                "Unable to retrieve project information for projectId={}",
                id
        );

        throw new RuntimeException(
                "Project Service is temporarily unavailable. " +
                "Unable to retrieve project information for projectId=" + id
        );
    }
    
}
