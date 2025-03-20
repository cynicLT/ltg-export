package org.cynic.ltg_export.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration.Type;
import org.cynic.ltg_export.client.ltg.ExecutionResult;
import org.cynic.ltg_export.client.ltg.ExecutionSummary;
import org.cynic.ltg_export.client.ltg.ExportListItem;
import org.cynic.ltg_export.client.ltg.MethodResultOfArrayOfExportListItem;
import org.cynic.ltg_export.client.ltg.MethodResultOfString;
import org.cynic.ltg_export.client.ltg.WaybillsImportServiceSoap;
import org.cynic.ltg_export.domain.ApplicationException;
import org.cynic.ltg_export.domain.model.CredentialsModel;
import org.cynic.ltg_export.domain.model.FilterModel;
import org.cynic.ltg_export.function.ThrowingFunction;
import org.springframework.stereotype.Component;

@Component
public class WebService {

    private final WaybillsImportServiceSoap waybillsImportServiceSoap;
    private final ObjectMapper objectMapper;
    private final Set<ReportExportConfiguration> configurations;

    public WebService(WaybillsImportServiceSoap waybillsImportServiceSoap, ObjectMapper objectMapper, Set<ReportExportConfiguration> configurations) {
        this.waybillsImportServiceSoap = waybillsImportServiceSoap;
        this.objectMapper = objectMapper;
        this.configurations = configurations;
    }

    public List<JsonNode> export(CredentialsModel credentials, String name, FilterModel filter) {
        return configurations.stream()
            .filter(it -> StringUtils.equals(it.name(), name))
            .findAny()
            .map(ReportExportConfiguration::type)
            .filter(Type.KR99::equals)
            .map(it -> kr99(credentials, filter))
            .orElseGet(() -> kr52(credentials, filter));
    }

    private List<JsonNode> kr52(CredentialsModel credentials, FilterModel filter) {
        String ticket = ticket(credentials);

        return kr52lines(ticket, filter).parallelStream().map(it -> kr52item(ticket, it)).flatMap(Optional::stream).map(this::convert).toList();
    }

    private Collection<Integer> kr52lines(String ticket, FilterModel filter) {
        return validate(
            waybillsImportServiceSoap.exportWaybillKR52(
                ticket,
                filter.paymentPeriod(),
                filter.changeDateFrom(),
                filter.changeDateTo(),
                filter.systemNumber(),
                filter.sendDate(),
                filter.departureDateFrom(),
                filter.departureDateTo(),
                filter.arrivalDateFrom(),
                filter.arrivalDateTo(),
                filter.number()
            ),
            MethodResultOfArrayOfExportListItem::getReturnValue).getExportListItem().stream().map(ExportListItem::getID).toList();
    }

    private List<JsonNode> kr99(CredentialsModel credentials, FilterModel filter) {
        String ticket = ticket(credentials);

        return kr99lines(ticket, filter).parallelStream().map(it -> kr99item(ticket, it)).flatMap(Optional::stream).map(this::convert).toList();
    }

    private String ticket(CredentialsModel credentials) {
        return validate(waybillsImportServiceSoap.getTicket(credentials.username(), credentials.password()), MethodResultOfString::getReturnValue);
    }

    private List<Integer> kr99lines(String ticket, FilterModel filter) {
        return validate(waybillsImportServiceSoap.exportWaybillKR99(
                ticket,
                filter.paymentPeriod(),
                filter.changeDateFrom(),
                filter.changeDateTo(),
                filter.systemNumber(),
                filter.sendDate(),
                filter.departureDateFrom(),
                filter.departureDateTo(),
                filter.arrivalDateFrom(),
                filter.arrivalDateTo(),
                filter.number()
            ),
            MethodResultOfArrayOfExportListItem::getReturnValue).getExportListItem().stream().map(ExportListItem::getID).toList();
    }

    private Optional<String> kr52item(String ticket, Integer id) {
        return filter(waybillsImportServiceSoap.exportWaybillKR52Item(ticket, id), MethodResultOfString::getReturnValue);

    }

    private Optional<String> kr99item(String ticket, Integer id) {
        return filter(waybillsImportServiceSoap.exportWaybillKR99Item(ticket, id), MethodResultOfString::getReturnValue);
    }

    private JsonNode convert(String item) {
        return ThrowingFunction.<String, JsonNode, Throwable>withTry(objectMapper::readTree,
                e -> new ApplicationException("error.ws.convert", Map.entry("item", item), Map.entry("message", ExceptionUtils.getRootCauseMessage(e))))
            .apply(item);
    }

    private <T extends ExecutionSummary, R> Optional<R> filter(T result, Function<T, R> extractor) {
        return Optional.of(result).filter(it -> ExecutionResult.EXECUTED.equals(it.getExecutionResult())).map(extractor);
    }

    private <T extends ExecutionSummary, R> R validate(T result, Function<T, R> extractor) {
        return filter(result, extractor)
            .orElseThrow(
                () -> new ApplicationException("error.ws.response", Map.entry("result", result.getExecutionResult()),
                    Map.entry("details", result.getExecutionDetails())));
    }
}
