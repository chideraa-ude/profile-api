package com.chideraau.profile_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProfileApiApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void testMeEndpoint_Returns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testMeEndpoint_ReturnsJson() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        assertThat(response.getHeaders().getContentType().toString())
                .contains("application/json");
    }

    @Test
    void testMeEndpoint_ContainsRequiredFields() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        String body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).contains("\"status\"");
        assertThat(body).contains("\"user\"");
        assertThat(body).contains("\"email\"");
        assertThat(body).contains("\"name\"");
        assertThat(body).contains("\"stack\"");
        assertThat(body).contains("\"timestamp\"");
        assertThat(body).contains("\"fact\"");
    }

    @Test
    void testMeEndpoint_StatusIsSuccess() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        assertThat(response.getBody()).contains("\"status\":\"success\"");
    }

    @Test
    void testMeEndpoint_TimestampIsDynamic() throws InterruptedException {
        ResponseEntity<String> response1 = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        Thread.sleep(100);
        
        ResponseEntity<String> response2 = restTemplate.getForEntity(
                "http://localhost:" + port + "/me",
                String.class
        );
        
        assertThat(response1.getBody()).isNotEqualTo(response2.getBody());
    }
}