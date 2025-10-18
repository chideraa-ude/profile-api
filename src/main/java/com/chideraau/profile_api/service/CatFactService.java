package com.chideraau.profile_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CatFactService {
    private static final Logger logger = LoggerFactory.getLogger(CatFactService.class);

    private static final String CAT_FACT_API_URL = "https://catfact.ninja/fact";
    private static final String FALLBACK_FACT = "Cats are amazing creatures!";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CatFactService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String getRandomCatFact() {
        try {
            logger.info("Fetching cat fact from: {}", CAT_FACT_API_URL);
            
            String response = restTemplate.getForObject(CAT_FACT_API_URL, String.class);
            
            if (response == null) {
                logger.warn("Received null response from Cat Facts API");
                return FALLBACK_FACT;
            }
            
            JsonNode jsonNode = objectMapper.readTree(response);
            String fact = jsonNode.get("fact").asText();
            
            logger.info("Successfully fetched cat fact");
            return fact;
            
        } catch (ResourceAccessException e) {
            logger.error("Network error while fetching cat fact: {}", e.getMessage());
            return FALLBACK_FACT;
            
        } catch (Exception e) {
            logger.error("Error fetching cat fact: {}", e.getMessage(), e);
            return FALLBACK_FACT;
        }
    }
}
