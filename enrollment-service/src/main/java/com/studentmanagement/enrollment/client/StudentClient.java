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
public class StudentClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.student.url}")
    private String studentServiceUrl;

    public Map<String, Object> getStudent(Long studentId) {
        return webClientBuilder.build()
                .get()
                .uri(studentServiceUrl + "/students/" + studentId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
