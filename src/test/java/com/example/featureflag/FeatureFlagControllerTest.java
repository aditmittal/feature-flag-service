package com.example.featureflag;

import com.example.featureflag.entity.Project;
import com.example.featureflag.repository.FeatureFlagRepository;
import com.example.featureflag.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureFlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @BeforeEach
    void setUp() {
        featureFlagRepository.deleteAll();
        projectRepository.deleteAll();

        projectRepository.save(
                new Project("payments", "Payments")
        );
    }

    @Test
    void shouldCreateFlag() throws Exception {
        mockMvc.perform(post("/projects/payments/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "checkout-v2",
                                  "state": "ON"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("checkout-v2"))
                .andExpect(jsonPath("$.state").value("ON"));
    }

    @Test
    void shouldGetFlag() throws Exception {
        createFlag();

        mockMvc.perform(
                        get("/projects/payments/flags/checkout-v2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("checkout-v2"))
                .andExpect(jsonPath("$.state").value("ON"));
    }

    @Test
    void shouldUpdateFlag() throws Exception {
        createFlag();

        mockMvc.perform(
                        put("/projects/payments/flags/checkout-v2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "state": "OFF"
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("checkout-v2"))
                .andExpect(jsonPath("$.state").value("OFF"));
    }

    @Test
    void shouldDeleteFlag() throws Exception {
        createFlag();

        mockMvc.perform(
                        delete("/projects/payments/flags/checkout-v2"))
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/projects/payments/flags/checkout-v2"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldNotExposeFlagFromAnotherProject() throws Exception {
        projectRepository.save(
                new Project("mobile", "Mobile")
        );

        createFlag();

        mockMvc.perform(
                        get("/projects/mobile/flags/checkout-v2"))
                .andExpect(status().is4xxClientError());
    }

    private void createFlag() throws Exception {
        mockMvc.perform(post("/projects/payments/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "checkout-v2",
                                  "state": "ON"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}