package com.chideraau.profile_api.controller;

import com.chideraau.profile_api.service.CatFactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ProfileController.class)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatFactService catFactService;

    @Test
    public void testGetProfile_ReturnsCorrectStructure() throws Exception {
        String mockCatFact = "Cats can rotate their ears 180 degrees.";
        when(catFactService.getRandomCatFact()).thenReturn(mockCatFact);

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.user.email").exists())
                .andExpect(jsonPath("$.user.name").exists())
                .andExpect(jsonPath("$.user.stack").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.fact").value(mockCatFact));
    }

    @Test
    public void testGetProfile_StatusIsSuccess() throws Exception {
        when(catFactService.getRandomCatFact()).thenReturn("Test cat fact");

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    public void testGetProfile_ReturnsValidTimestamp() throws Exception {
        when(catFactService.getRandomCatFact()).thenReturn("Test cat fact");

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    public void testGetProfile_CallsCatFactService() throws Exception {
        String expectedFact = "Cats are amazing creatures!";
        when(catFactService.getRandomCatFact()).thenReturn(expectedFact);

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fact").value(expectedFact));
    }
}
