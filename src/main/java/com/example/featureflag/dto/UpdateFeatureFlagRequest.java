package com.example.featureflag.dto;

import com.example.featureflag.entity.FlagState;
import jakarta.validation.constraints.NotNull;

public record UpdateFeatureFlagRequest(@NotNull FlagState state) {

}
