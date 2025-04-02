package org.cynic.ltg_export.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minidev.json.JSONArray;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cynic.ltg_export.Configuration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration.FieldReportExportConfiguration;
import org.cynic.ltg_export.domain.ApplicationException;
import org.cynic.ltg_export.function.ThrowingConsumer;
import org.cynic.ltg_export.function.ThrowingFunction;
import org.springframework.stereotype.Component;

@Component
public class ExportService {

    private final Function<OutputStream, CSVPrinter> csvPrinter;
    BiFunction<String, String, String> expression;
    private final Set<Configuration.ExportConfiguration.ReportExportConfiguration> configurations;

    public ExportService(
        Function<OutputStream, CSVPrinter> csvPrinter,
        BiFunction<String, String, String> expression,
        Set<Configuration.ExportConfiguration.ReportExportConfiguration> configurations) {

        this.csvPrinter = csvPrinter;
        this.expression = expression;
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
            printer.printRecord(header(configuration));

            items.parallelStream().map(it -> line(it, configuration))
                .forEach(ThrowingConsumer.withTry(
                    printer::printRecord,
                    e -> new ApplicationException("error.export.print",
                        Map.entry("message", ExceptionUtils.getRootCauseMessage(e))
                    )
                ));

            return outputStream::toByteArray;
        } catch (IOException e) {
            throw new ApplicationException("error.export",
                Map.entry("message", ExceptionUtils.getRootCauseMessage(e))
            );
        }
    }

    private List<String> line(JsonNode item, ReportExportConfiguration configuration) {
        return configuration.fields()
            .stream()
            .sorted(Comparator.comparingInt(FieldReportExportConfiguration::index))
            .map(it -> parse(item, it))
            .toList();
    }

    private static List<String> header(ReportExportConfiguration configuration) {
        return configuration.fields()
            .stream()
            .sorted(Comparator.comparingInt(FieldReportExportConfiguration::index))
            .map(FieldReportExportConfiguration::label)
            .toList();
    }

    private String parse(JsonNode item, FieldReportExportConfiguration field) {
        return Optional.of(item)
            .map(JsonNode::toString)
            .map(ThrowingFunction.withTry(
                it -> JsonPath.<Object>read(it, field.path()),
                () -> StringUtils.EMPTY
            ))
            .map(result -> Optional.of(result)
                .filter(it -> ClassUtils.isAssignable(it.getClass(), JSONArray.class))
                .map(it -> JSONArray.class.cast(it)
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("; "))
                )
                .orElseGet(() -> Optional.of(result)
                    .map(Object::toString)
                    .orElse(StringUtils.EMPTY)
                )
            )
            .map(Object::toString)
            .map(it -> mutate(field, it))
            .orElse(StringUtils.EMPTY);
    }

    private String mutate(FieldReportExportConfiguration field, String value) {
        Boolean filter = Optional.of(field)
            .map(FieldReportExportConfiguration::filter)
            .map(StringUtils::trimToNull)
            .map(it -> expression.apply(it, value))
            .map(BooleanUtils::toBoolean)
            .orElse(Boolean.TRUE);

        return Optional.of(filter)
            .filter(Boolean.TRUE::equals)
            .map(it -> Optional.of(field)
                .map(FieldReportExportConfiguration::transform)
                .map(StringUtils::trimToNull)
                .map(t -> expression.apply(t, value))
                .map(Object::toString)
                .orElse(value)
            ).orElse(StringUtils.EMPTY);
    }
}