package org.cynic.ltg_export.framework.environment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.cynic.ltg_export.domain.ApplicationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public final class SingleActiveProfileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Integer MAX_ACTIVE_PROFILES = 1;

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());

        if (!MAX_ACTIVE_PROFILES.equals(activeProfiles.size())) {
            throw new ApplicationException(
                "error.multiple.profiles.active",
                Map.entry("maxActiveProfiles", MAX_ACTIVE_PROFILES),
                Map.entry("profiles", activeProfiles)
            );
        }
    }
}
