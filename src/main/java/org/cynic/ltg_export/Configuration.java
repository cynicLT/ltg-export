package org.cynic.ltg_export;

import jakarta.xml.ws.BindingProvider;
import org.apache.commons.lang3.StringUtils;
import org.cynic.ltg_export.client.ExecutionResult;
import org.cynic.ltg_export.client.MethodResultOfString;
import org.cynic.ltg_export.client.WaybillsImportService;
import org.cynic.ltg_export.client.WaybillsImportServiceSoap;
import org.cynic.ltg_export.domain.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.net.URL;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@SpringBootConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration({
})
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {TypeExcludeFilter.class}),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {AutoConfigurationExcludeFilter.class})
})
@EnableConfigurationProperties(
        Configuration.WaybillsImportServiceConfiguration.class
)
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    public Configuration() {
        Optional.of(LOGGER)
                .filter(it -> it.isInfoEnabled(Constants.AUDIT_MARKER))
                .map(_ -> getClass())
                .map(Class::getPackage)
                .ifPresent(it -> LOGGER.info(Constants.AUDIT_MARKER,
                                "[{}-{}] STARTED",
                                it.getImplementationTitle(),
                                it.getImplementationVersion()
                        )
                );
    }

    @Bean
    public Clock clock() {
        return Clock.system(Constants.SYSTEM_ZONE_ID);
    }

    @Bean
    public WaybillsImportServiceSoap waybillsImportServiceSoap(WaybillsImportServiceConfiguration configuration) {
        WaybillsImportService waybillsImportService = new WaybillsImportService();
        WaybillsImportServiceSoap waybillsImportServiceSoap = waybillsImportService.getWaybillsImportServiceSoap();
        BindingProvider.class.cast(waybillsImportServiceSoap).getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                configuration.endpoint()
        );

        return waybillsImportServiceSoap;
    }

    @Bean
    public Supplier<String> ticket(WaybillsImportServiceSoap waybillsImportServiceSoap, WaybillsImportServiceConfiguration configuration) {
        return () -> Optional.of(waybillsImportServiceSoap)
                .map(it -> it.getTicket(configuration.username(), configuration.password()))
                .map(result -> Optional.of(result)
                        .filter(it -> ExecutionResult.EXECUTED.equals(it.getExecutionResult()))
                        .orElseThrow(
                                () -> new ApplicationException("error.ticket",
                                        Map.entry("result", result.getExecutionResult()),
                                        Map.entry("details", result.getExecutionDetails())
                                )
                        )
                )
                .map(MethodResultOfString::getReturnValue)
                .map(StringUtils::trimToNull)
                .orElseThrow(
                        () -> new ApplicationException("error.ticket.empty")
                );
    }


    @ConfigurationProperties(prefix = "webservice.ltg.waybills-import")
    public record WaybillsImportServiceConfiguration(String endpoint, String username, String password) {

    }
}