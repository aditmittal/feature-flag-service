package com.example.featureflag;

import com.example.featureflag.entity.FeatureFlag;
import com.example.featureflag.entity.FlagState;
import com.example.featureflag.entity.Project;
import com.example.featureflag.repository.FeatureFlagRepository;
import com.example.featureflag.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class FeatureFlagRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Test
    void shouldFindFlagForProject() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.ON,
                project
        );
        featureFlagRepository.save(flag);

        var result = featureFlagRepository
                .findByProjectIdAndName("payments", "checkout-v2");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("checkout-v2");
        assertThat(result.get().getState()).isEqualTo(FlagState.ON);
    }

    @Test
    void shouldNotReturnFlagFromAnotherProject() {
        Project payments = new Project("payments", "Payments");
        Project mobile = new Project("mobile", "Mobile");

        projectRepository.save(payments);
        projectRepository.save(mobile);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.ON,
                payments
        );
        featureFlagRepository.save(flag);

        var result = featureFlagRepository
                .findByProjectIdAndName("mobile", "checkout-v2");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckWhetherFlagExistsForProject() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.OFF,
                project
        );
        featureFlagRepository.save(flag);

        assertThat(
                featureFlagRepository
                        .existsByProjectIdAndName("payments", "checkout-v2")
        ).isTrue();

        assertThat(
                featureFlagRepository
                        .existsByProjectIdAndName("payments", "unknown")
        ).isFalse();
    }
}