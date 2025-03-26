package org.cynic.ltg_export;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.ws.BindingProvider;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.client.ltg.WaybillsImportService;
import org.cynic.ltg_export.client.ltg.WaybillsImportServiceSoap;
import org.cynic.ltg_export.domain.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SpringBootConfiguration(proxyBeanMethods = false)
@ImportAutoConfiguration({
  //        Configuration properties
  ConfigurationPropertiesAutoConfiguration.class,

  //    Jackson configuration
  JacksonAutoConfiguration.class,

})
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.CUSTOM, classes = {TypeExcludeFilter.class}),
  @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {AutoConfigurationExcludeFilter.class})})
@EnableConfigurationProperties({Configuration.ExportConfiguration.class})
public class Configuration {

  private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

  public Configuration() {
    Optional.of(LOGGER).filter(it -> it.isInfoEnabled(Constants.AUDIT_MARKER)).map(it -> getClass()).map(Class::getPackage)
      .ifPresent(it -> LOGGER.info(Constants.AUDIT_MARKER, "[{}-{}] STARTED", it.getImplementationTitle(), it.getImplementationVersion()));
  }

  @Bean
  public Clock clock() {
    return Clock.system(Constants.SYSTEM_ZONE_ID);
  }

  @Bean
  public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(true).build();
  }

  @Bean
  public WaybillsImportServiceSoap waybillsImportServiceSoap(@Value("${webservice.ltg.waybills-import.endpoint}") String endpoint) {
    WaybillsImportService waybillsImportService = new WaybillsImportService();
    WaybillsImportServiceSoap waybillsImportServiceSoap = waybillsImportService.getWaybillsImportServiceSoap();
    BindingProvider.class.cast(waybillsImportServiceSoap).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

    return waybillsImportServiceSoap;
  }

  @Bean
  public BiConsumer<String, byte[]> writer() {
    return (fileName, data) -> {

      try {
        Files.write(Path.of(fileName), data);
      } catch (IOException e) {
        throw new ApplicationException("error.writer.io", Map.entry("fileName", fileName), Map.entry("size", ArrayUtils.getLength(data)),
          Map.entry("message", ExceptionUtils.getRootCauseMessage(e)));
      }
    };
  }

  @Bean
  public Function<OutputStream, CSVPrinter> csvPrinter() {
    return outputStream -> {
      try {
        return new CSVPrinter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
          CSVFormat.Builder.create(CSVFormat.EXCEL)
            .setQuoteMode(QuoteMode.ALL)
            .build());
      } catch (IOException e) {
        throw new ApplicationException("error.csv-printer", Map.entry("message", ExceptionUtils.getRootCauseMessage(e)));
      }
    };
  }

  @ConfigurationProperties(prefix = "export")
  public static class ExportConfiguration extends HashSet<ReportExportConfiguration> {

    public record ReportExportConfiguration(String name, Type type, Set<FieldReportExportConfiguration> fields) {

      public enum Type {
        KR99, KR52
      }

      public record FieldReportExportConfiguration(Integer index, String label, String path) {

      }
    }
  }
}