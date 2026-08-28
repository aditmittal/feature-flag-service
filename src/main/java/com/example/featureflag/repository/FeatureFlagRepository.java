package com.example.featureflag.repository;

import com.example.featureflag.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByProjectIdAndName(
            String projectId,
            String name
    );

    boolean existsByProjectIdAndName(
            String projectId,
            String name
    );

    List<FeatureFlag> findAllByProjectId(
            String projectId
    );
}
