package com.example.featureflag.dto;

import com.example.featureflag.entity.FlagState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeatureFlagRequest(
        @NotBlank String name,
        @NotNull FlagState state
) {
}