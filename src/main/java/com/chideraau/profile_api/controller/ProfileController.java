package com.chideraau.profile_api.controller;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;  // ← ADD THIS IMPORT
import org.springframework.web.bind.annotation.RestController;

import com.chideraau.profile_api.model.ProfileResponse;
import com.chideraau.profile_api.service.CatFactService;

@RestController
public class ProfileController {
    
    @Autowired
    private CatFactService catFactService;

    @GetMapping("/me")  // ← ADD THIS ANNOTATION
    public ResponseEntity<ProfileResponse> getProfile() {
        // Fetch cat fact
        String catFact = catFactService.getRandomCatFact();
        
        // Get current timestamp
        String timestamp = Instant.now().toString();
        
        // Build the nested UserInfo object FIRST
        ProfileResponse.UserInfo userInfo = ProfileResponse.UserInfo.builder()
                .email("chideraaude@gmail.com")
                .name("Chideraa Ude")
                .stack("Java/Spring Boot")
                .build();
        
        // Build the complete response with the UserInfo object
        ProfileResponse response = ProfileResponse.builder()
                .status("success")
                .user(userInfo)  // ← Pass the UserInfo object here
                .timestamp(timestamp)
                .fact(catFact)
                .build();

        return ResponseEntity.ok(response);
    }
}