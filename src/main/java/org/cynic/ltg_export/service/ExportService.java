package org.cynic.ltg_export.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.cynic.ltg_export.Configuration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration.FieldReportExportConfiguration;
import org.cynic.ltg_export.Constants;
import org.cynic.ltg_export.domain.ApplicationException;
import org.cynic.ltg_export.function.ThrowingFunction;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExportService {

  private final Function<OutputStream, CSVPrinter> csvPrinter;
  private final Set<Configuration.ExportConfiguration.ReportExportConfiguration> configurations;

  private final Map<String, Function<Object, Object>> AGGREGATE_FUNCTIONS =
    Map.ofEntries(
      Map.entry(".all()", this::allAggregate),
      Map.entry(".sum()", this::sumAggregate),
      Map.entry(".distinct()", this::distinctAggregate)
    );

  public ExportService(Function<OutputStream, CSVPrinter> csvPrinter, Set<Configuration.ExportConfiguration.ReportExportConfiguration> configurations) {
    this.csvPrinter = csvPrinter;
    this.configurations = configurations;
  }

  public Supplier<byte[]> export(
    String name, List<JsonNode> items) {
    ReportExportConfiguration configuration =
      configurations.stream()
        .filter(it -> StringUtils.equals(it.name(), name))
        .findAny()
        .orElseThrow(
          () -> new ApplicationException("error.export.not-supported", Map.entry("name", name))
        );

    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         CSVPrinter printer = csvPrinter.apply(outputStream)) {

      List<List<String>> lines = Stream.concat(
        Stream.of(
          configuration.fields()
            .stream()
            .sorted(Comparator.comparingInt(FieldReportExportConfiguration::index))
            .map(FieldReportExportConfiguration::label)
            .toList()
        ),
        items.stream()
          .map(item -> configuration.fields()
            .stream()
            .sorted(Comparator.comparingInt(FieldReportExportConfiguration::index))
            .map(FieldReportExportConfiguration::path)
            .map(it -> parse(item, it))
            .map(it -> Objects.toString(it, StringUtils.EMPTY))
            .toList()
          )
      ).toList();

      printer.printRecords(lines);

      return outputStream::toByteArray;
    } catch (IOException e) {
      throw new ApplicationException("error.export",
        Map.entry("message", ExceptionUtils.getRootCauseMessage(e))
      );
    }
  }

  private String distinctAggregate(Object item) {
    return Optional.ofNullable(item)
      .filter(it -> ClassUtils.isAssignable(it.getClass(), JSONArray.class))
      .map(JSONArray.class::cast)
      .stream()
      .flatMap(Collection::stream)
      .map(Object::toString)
      .distinct()
      .collect(Collectors.joining(";"));
  }

  private BigDecimal sumAggregate(Object item) {
    return Optional.ofNullable(item)
      .filter(it -> ClassUtils.isAssignable(it.getClass(), JSONArray.class))
      .map(JSONArray.class::cast)
      .stream()
      .flatMap(Collection::stream)
      .map(Object::toString)
      .filter(NumberUtils::isCreatable)
      .map(NumberUtils::createBigDecimal)
      .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(Constants.SCALE, RoundingMode.HALF_UP);
  }

  private String allAggregate(Object item) {
    return Optional.ofNullable(item)
      .filter(it -> ClassUtils.isAssignable(it.getClass(), JSONArray.class))
      .map(JSONArray.class::cast)
      .stream()
      .flatMap(Collection::stream)
      .map(Object::toString)
      .collect(Collectors.joining(","));
  }

  private Object parse(JsonNode json, String path) {
    return AGGREGATE_FUNCTIONS.entrySet()
      .stream()
      .filter(it -> StringUtils.endsWith(path, it.getKey()))
      .findAny()
      .map(it ->
        Map.entry(StringUtils.substringBeforeLast(path, it.getKey()), it.getValue())
      )
      .or(() -> Optional.of(Map.entry(path, object -> object))
      )
      .map(ThrowingFunction.withTry(
        it -> it.getValue().apply(JsonPath.read(json.toString(), it.getKey())),
        () -> null
      ))
      .orElse(null);
  }
}