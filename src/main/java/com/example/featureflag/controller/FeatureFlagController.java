package com.example.featureflag.controller;

import com.example.featureflag.dto.CreateFeatureFlagRequest;
import com.example.featureflag.dto.FeatureFlagResponse;
import com.example.featureflag.dto.UpdateFeatureFlagRequest;
import com.example.featureflag.service.FeatureFlagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/{projectId}/flags")
public class FeatureFlagController {
    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeatureFlagResponse create(
            @PathVariable String projectId,
            @Valid @RequestBody CreateFeatureFlagRequest request
            ){
        return featureFlagService.create(projectId, request);
    }

    @GetMapping
    public List<FeatureFlagResponse> findAll(
            @PathVariable String projectId
    ){
        return featureFlagService.findAll(projectId);
    }

    @GetMapping("/{name}")
    public FeatureFlagResponse find(
            @PathVariable String projectId,
            @PathVariable String name
    ){
        return featureFlagService.find(projectId, name);
    }

    @PutMapping("/{name}")
    public FeatureFlagResponse update(
            @PathVariable String projectId,
            @PathVariable String name,
            @Valid @RequestBody UpdateFeatureFlagRequest request
            ){
        return featureFlagService.update(projectId, name, request);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> delete(
            @PathVariable String projectId,
            @PathVariable String name
    ) {
        featureFlagService.delete(projectId, name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eval")
    public String evaluate(
            @PathVariable String projectId,
            @RequestParam String flag,
            @RequestParam String user
    ) {
        return featureFlagService.evaluate(
                projectId,
                flag,
                user
        );
    }

}
