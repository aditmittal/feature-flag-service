package com.example.featureflag.dto;

import com.example.featureflag.entity.FlagState;

public record FeatureFlagResponse(
        String name,
        FlagState state
) {
}
