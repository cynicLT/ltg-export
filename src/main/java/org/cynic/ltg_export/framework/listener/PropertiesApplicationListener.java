package org.cynic.ltg_export.framework.listener;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.cynic.ltg_export.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public final class PropertiesApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesApplicationListener.class);
    private static final Set<Class<?>> PROPERTY_SOURCE_CLASSES = Set.of(SystemEnvironmentPropertySource.class,
        OriginTrackedMapPropertySource.class);

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent applicationReadyEvent) {
        AbstractEnvironment environment = (AbstractEnvironment) applicationReadyEvent.getApplicationContext()
            .getEnvironment();

        Optional.of(LOGGER)
            .filter(it -> it.isInfoEnabled(Constants.AUDIT_MARKER))
            .map(it -> resolveMessage(environment))
            .ifPresent(it -> LOGGER.info(Constants.AUDIT_MARKER, it));
    }

    private String resolveMessage(AbstractEnvironment environment) {
        return environment.getPropertySources()
            .stream()
            .filter(this::isLoggable)
            .map(MapPropertySource.class::cast)
            .map(PropertySource::getSource)
            .flatMap(it -> it.keySet().stream())
            .distinct()
            .map(it -> it + "=" + environment.getProperty(it))
            .collect(Collectors.joining(System.lineSeparator(), System.lineSeparator(), StringUtils.SPACE));
    }

    private boolean isLoggable(PropertySource<?> propertySource) {
        return PROPERTY_SOURCE_CLASSES.stream()
            .anyMatch(it -> it.isAssignableFrom(propertySource.getClass()));
    }
}
