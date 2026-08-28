package com.example.featureflag;

import com.example.featureflag.entity.FeatureFlag;
import com.example.featureflag.entity.FlagState;
import com.example.featureflag.entity.Project;
import com.example.featureflag.repository.FeatureFlagRepository;
import com.example.featureflag.repository.ProjectRepository;
import com.example.featureflag.service.FeatureFlagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(FeatureFlagService.class)
class FeatureFlagEvaluationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FeatureFlagRepository featureFlagRepository;

    @Autowired
    private FeatureFlagService featureFlagService;

    @BeforeEach
    void setUp() {
        featureFlagRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void shouldReturnOnWhenFlagIsOn() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.ON,
                project
        );
        featureFlagRepository.save(flag);

        String result = featureFlagService.evaluate(
                "payments",
                "checkout-v2",
                "alice"
        );

        assertThat(result).isEqualTo("on");
    }

    @Test
    void shouldReturnOffWhenFlagIsOff() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.OFF,
                project
        );
        featureFlagRepository.save(flag);

        String result = featureFlagService.evaluate(
                "payments",
                "checkout-v2",
                "alice"
        );

        assertThat(result).isEqualTo("off");
    }

    @Test
    void shouldReturnStableResultForSameUserAndFlag() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        FeatureFlag flag = new FeatureFlag(
                "checkout-v2",
                FlagState.DEFAULT,
                project
        );
        featureFlagRepository.save(flag);

        String firstResult = featureFlagService.evaluate(
                "payments",
                "checkout-v2",
                "alice"
        );

        String secondResult = featureFlagService.evaluate(
                "payments",
                "checkout-v2",
                "alice"
        );

        assertThat(firstResult).isEqualTo(secondResult);
    }

    @Test
    void shouldNotEvaluateFlagFromAnotherProject() {
        Project payments = new Project("payments", "Payments");
        Project mobile = new Project("mobile", "Mobile");

        projectRepository.save(payments);
        projectRepository.save(mobile);

        FeatureFlag paymentsFlag = new FeatureFlag(
                "checkout-v2",
                FlagState.ON,
                payments
        );

        featureFlagRepository.save(paymentsFlag);

        assertThatThrownBy(() ->
                featureFlagService.evaluate(
                        "mobile",
                        "checkout-v2",
                        "alice"
                )
        )
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowWhenFlagDoesNotExist() {
        Project project = new Project("payments", "Payments");
        projectRepository.save(project);

        assertThatThrownBy(() ->
                featureFlagService.evaluate(
                        "payments",
                        "checkout-v2",
                        "alice"
                )
        )
                .isInstanceOf(RuntimeException.class);
    }
}