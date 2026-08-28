package com.example.featureflag.controller;

import com.example.featureflag.service.FeatureFlagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects/{projectId}")
public class FeatureFlagEvaluationController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagEvaluationController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @GetMapping("/eval")
    public String evaluate(
            @PathVariable String projectId,
            @RequestParam String flag,
            @RequestParam String user
    ) {
        return featureFlagService.evaluate(projectId, flag, user);
    }
}