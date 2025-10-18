package com.chideraau.profile_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model representing the JSON structure returned by /me endpoint
 * 
 * @Data - Lombok annotation that generates getters, setters, toString, equals, hashCode
 * @Builder - Allows building objects fluently
 * @AllArgsConstructor/@NoArgsConstructor - Generates constructors
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    
    private String status;
    
    @JsonProperty("user")
    private UserInfo user;
    
    private String timestamp;
    private String fact;
    
    /**
     * Inner class for user information
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private String email;
        private String name;
        private String stack;
    }
}
