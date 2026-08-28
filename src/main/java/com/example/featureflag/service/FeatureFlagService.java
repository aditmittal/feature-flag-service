package com.example.featureflag.service;

import com.example.featureflag.dto.CreateFeatureFlagRequest;
import com.example.featureflag.dto.FeatureFlagResponse;
import com.example.featureflag.dto.UpdateFeatureFlagRequest;
import com.example.featureflag.entity.FeatureFlag;
import com.example.featureflag.entity.Project;
import com.example.featureflag.exception.ResourceNotFoundException;
import com.example.featureflag.repository.FeatureFlagRepository;
import com.example.featureflag.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final ProjectRepository projectRepository;

    public FeatureFlagService(FeatureFlagRepository featureFlagRepository, ProjectRepository projectRepository) {
        this.featureFlagRepository = featureFlagRepository;
        this.projectRepository = projectRepository;
    }


    public FeatureFlagResponse create(
            String projectId,
            CreateFeatureFlagRequest request
    ){
        Project project = projectRepository.findById(projectId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Project not found"));

        if(featureFlagRepository.existsByProjectIdAndName(projectId, request.name())){
            throw new IllegalArgumentException("Feature flag already exists");
        }

        FeatureFlag flag = new FeatureFlag(
                request.name(),
                request.state(),
                project
        );

        FeatureFlag saved = featureFlagRepository.save(flag);

        return toResponse(saved);
    }


    public List<FeatureFlagResponse> findAll(String projectId){
        return featureFlagRepository.
                findAllByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FeatureFlagResponse find(
            String projectId,
            String name
    ){
        FeatureFlag flag = featureFlagRepository
                .findByProjectIdAndName(projectId, name).orElseThrow(
                        () -> new ResourceNotFoundException("feature flag not found")
                );
        return toResponse(flag);
    }

    public void delete(String projectId, String name){
        FeatureFlag flag = featureFlagRepository.findByProjectIdAndName(projectId, name).orElseThrow(() -> 
                new ResourceNotFoundException("Feature flag not found"));

        featureFlagRepository.delete(flag);
    }

    public FeatureFlagResponse update(
            String projectId,
            String name,
            UpdateFeatureFlagRequest request
    ){
        FeatureFlag flag = featureFlagRepository.findByProjectIdAndName(projectId, name)
                .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found"));

        flag.setState(request.state());

        return toResponse(featureFlagRepository.save(flag));
    }


    private FeatureFlagResponse toResponse(FeatureFlag flag){
        return new FeatureFlagResponse(
                flag.getName(),
                flag.getState()
        );
    }

}
