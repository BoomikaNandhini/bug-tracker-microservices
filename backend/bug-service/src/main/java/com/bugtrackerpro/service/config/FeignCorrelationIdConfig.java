package com.bugtrackerpro.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;

@Configuration 
public class FeignCorrelationIdConfig {
   private static final String CORRELATION_ID = "X-Correlation-Id";


    @Bean
    public RequestInterceptor correlationIdInterceptor() {

        return requestTemplate -> {

            RequestAttributes attributes =
                    RequestContextHolder.getRequestAttributes();

            if (attributes instanceof ServletRequestAttributes servletAttributes) {

                String correlationId =
                        servletAttributes.getRequest()
                                .getHeader(CORRELATION_ID);

                if (correlationId != null && !correlationId.isBlank()) {
                    requestTemplate.header(
                            CORRELATION_ID,
                            correlationId
                    );
                }
            }
        };
    }
}
