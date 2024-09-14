package com.inventoryservice.inventoryservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventoryservice.inventoryservice.Dto.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class AuthenticationServiceClient {

    private final RestTemplate restTemplate;

    @Value("${authentication.service.url}")
    private String authServiceUrl;


    public AuthenticationServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(authServiceUrl + "/validate", HttpMethod.GET, entity, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(authServiceUrl + "/extract-username", HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> extractRoles(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call the correct endpoint that returns roles
            ResponseEntity<String> response = restTemplate.exchange(
                    authServiceUrl + "/extract-username", HttpMethod.GET, entity, String.class
            );

            // Use ObjectMapper to parse JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) responseBody.get("roles");

            return roles != null ? roles : Collections.emptyList();
        } catch (Exception e) {
            // Handle exceptions and return an empty list
            return Collections.emptyList();
        }
    }

}
