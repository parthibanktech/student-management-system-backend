package com.studentmanagement.enrollment.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.course.url}")
    private String courseServiceUrl;

    public Map<String, Object> getCourse(Long courseId) {
        return webClientBuilder.build()
                .get()
                .uri(courseServiceUrl + "/courses/" + courseId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
