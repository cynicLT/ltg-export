package org.cynic.ltg_export.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration.Type;
import org.cynic.ltg_export.client.ltg.ArrayOfExportListItem;
import org.cynic.ltg_export.client.ltg.ExecutionResult;
import org.cynic.ltg_export.client.ltg.ExecutionSummary;
import org.cynic.ltg_export.client.ltg.ExportListItem;
import org.cynic.ltg_export.client.ltg.MethodResultOfArrayOfExportListItem;
import org.cynic.ltg_export.client.ltg.MethodResultOfString;
import org.cynic.ltg_export.client.ltg.WaybillsImportServiceSoap;
import org.cynic.ltg_export.domain.ApplicationException;
import org.cynic.ltg_export.domain.model.CredentialsModel;
import org.cynic.ltg_export.domain.model.FilterModel;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class WebServiceTest {

  private WebService webService;

  @Mock
  private WaybillsImportServiceSoap waybillsImportServiceSoap;

  @Mock
  private ObjectMapper objectMapper;

  private final String nameKR52 = Instancio.create(String.class);
  private final String nameKR99 = Instancio.create(String.class);
  private final Set<ReportExportConfiguration> configurations = Set.of(
    Instancio.of(ReportExportConfiguration.class)
      .set(Select.field("name"), nameKR52)
      .set(Select.field("type"), Type.KR52)
      .create(),
    Instancio.of(ReportExportConfiguration.class)
      .set(Select.field("name"), nameKR99)
      .set(Select.field("type"), Type.KR99)
      .create()
  );

  @BeforeEach
  void setUp() {
    this.webService = new WebService(waybillsImportServiceSoap, objectMapper, configurations);
  }

  @Test
  void exportWhenKr52Ok() throws JsonProcessingException {
    CredentialsModel credentials = Instancio.create(CredentialsModel.class);
    FilterModel filter = Instancio.create(FilterModel.class);
    String ticket = Instancio.create(String.class);
    ExportListItem item = Instancio.create(ExportListItem.class);
    ArrayOfExportListItem items = new ArrayOfExportListItem() {{
      getExportListItem().add(item);
    }};

    MethodResultOfString ticketResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field("returnValue"), ticket)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfArrayOfExportListItem linesResponse = Instancio.of(MethodResultOfArrayOfExportListItem.class)
      .set(Select.field("returnValue"), items)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfString itemResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();
    JsonNode itemJson = Instancio.create(ObjectNode.class);

    Mockito.when(waybillsImportServiceSoap.getTicket(credentials.username(), credentials.password())).thenReturn(
      ticketResponse
    );

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR52(ticket,
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
      ))
      .thenReturn(linesResponse);

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR52Item(ticket, item.getID()))
      .thenReturn(itemResponse);

    Mockito.when(objectMapper.readTree(itemResponse.getReturnValue())).thenReturn(itemJson);

    Assertions.assertThat(webService.export(credentials, nameKR52, filter))
      .containsExactly(itemJson);

    Mockito.verify(waybillsImportServiceSoap).getTicket(credentials.username(), credentials.password());
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR52(ticket,
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
    );
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR52Item(ticket, item.getID());
    Mockito.verify(objectMapper).readTree(itemResponse.getReturnValue());

    Mockito.verifyNoMoreInteractions(waybillsImportServiceSoap, objectMapper);
  }

  @Test
  void exportWhenKr99Ok() throws JsonProcessingException {
    CredentialsModel credentials = Instancio.create(CredentialsModel.class);
    FilterModel filter = Instancio.create(FilterModel.class);
    String ticket = Instancio.create(String.class);

    ExportListItem item = Instancio.create(ExportListItem.class);
    ArrayOfExportListItem items = new ArrayOfExportListItem() {{
      getExportListItem().add(item);
    }};

    MethodResultOfString ticketResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field("returnValue"), ticket)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfArrayOfExportListItem linesResponse = Instancio.of(MethodResultOfArrayOfExportListItem.class)
      .set(Select.field("returnValue"), items)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfString itemResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();
    JsonNode itemJson = Instancio.create(ObjectNode.class);

    Mockito.when(waybillsImportServiceSoap.getTicket(credentials.username(), credentials.password())).thenReturn(
      ticketResponse
    );

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR99(ticket,
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
      ))
      .thenReturn(linesResponse);

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR99Item(ticket, item.getID()))
      .thenReturn(itemResponse);

    Mockito.when(objectMapper.readTree(itemResponse.getReturnValue())).thenReturn(itemJson);

    Assertions.assertThat(webService.export(credentials, nameKR99, filter))
      .containsExactly(itemJson);

    Mockito.verify(waybillsImportServiceSoap).getTicket(credentials.username(), credentials.password());
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR99(ticket,
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
    );
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR99Item(ticket, item.getID());
    Mockito.verify(objectMapper).readTree(itemResponse.getReturnValue());

    Mockito.verifyNoMoreInteractions(waybillsImportServiceSoap, objectMapper);
  }

  @Test
  void exportWhenErrorConvert() throws JsonProcessingException {
    String ticket = Instancio.create(String.class);
    CredentialsModel credentials = Instancio.create(CredentialsModel.class);
    FilterModel filter = Instancio.create(FilterModel.class);
    ExportListItem item = Instancio.create(ExportListItem.class);
    ArrayOfExportListItem items = new ArrayOfExportListItem() {{
      getExportListItem().add(item);
    }};

    MethodResultOfString ticketResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field("returnValue"), ticket)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfArrayOfExportListItem linesResponse = Instancio.of(MethodResultOfArrayOfExportListItem.class)
      .set(Select.field("returnValue"), items)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfString itemResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    JsonProcessingException cause = Instancio.create(JsonProcessingException.class);

    Mockito.when(waybillsImportServiceSoap.getTicket(credentials.username(), credentials.password())).thenReturn(
      ticketResponse
    );

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR99(ticket,
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
      ))
      .thenReturn(linesResponse);

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR99Item(ticket, item.getID()))
      .thenReturn(itemResponse);

    Mockito.when(objectMapper.readTree(itemResponse.getReturnValue())).thenThrow(cause);

    Assertions.assertThatThrownBy(() -> webService.export(credentials, nameKR99, filter))
      .asInstanceOf(InstanceOfAssertFactories.throwable(ApplicationException.class))
      .matches(it -> StringUtils.equals(it.getCode(), "error.ws.convert"))
      .extracting(ApplicationException::getValues)
      .asInstanceOf(InstanceOfAssertFactories.MAP)
      .containsOnly(Map.entry("item", itemResponse.getReturnValue()),
        Map.entry("message", ExceptionUtils.getRootCauseMessage(cause)));

    Mockito.verify(waybillsImportServiceSoap).getTicket(credentials.username(), credentials.password());
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR99(ticket,
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
    );
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR99Item(ticket, item.getID());
    Mockito.verify(objectMapper).readTree(itemResponse.getReturnValue());

    Mockito.verifyNoMoreInteractions(waybillsImportServiceSoap, objectMapper);
  }

  @Test
  void exportWhenErrorValidate() {
    String ticket = Instancio.create(String.class);
    CredentialsModel credentials = Instancio.create(CredentialsModel.class);
    FilterModel filter = Instancio.create(FilterModel.class);

    MethodResultOfString ticketResponse = Instancio.of(MethodResultOfString.class)
      .set(Select.field("returnValue"), ticket)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXECUTED)
      .create();

    MethodResultOfArrayOfExportListItem linesResponse = Instancio.of(MethodResultOfArrayOfExportListItem.class)
      .set(Select.field(ExecutionSummary.class, "executionResult"), ExecutionResult.EXCEPTION_OCCURED)
      .create();

    Mockito.when(waybillsImportServiceSoap.getTicket(credentials.username(), credentials.password())).thenReturn(
      ticketResponse
    );

    Mockito.when(waybillsImportServiceSoap.exportWaybillKR99(ticket,
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
      ))
      .thenReturn(linesResponse);

    Assertions.assertThatThrownBy(() -> webService.export(credentials, nameKR99, filter))
      .asInstanceOf(InstanceOfAssertFactories.throwable(ApplicationException.class))
      .matches(it -> StringUtils.equals(it.getCode(), "error.ws.response"))
      .extracting(ApplicationException::getValues)
      .asInstanceOf(InstanceOfAssertFactories.MAP)
      .containsOnly(Map.entry("result", linesResponse.getExecutionResult()),
        Map.entry("details", linesResponse.getExecutionDetails()));

    Mockito.verify(waybillsImportServiceSoap).getTicket(credentials.username(), credentials.password());
    Mockito.verify(waybillsImportServiceSoap).exportWaybillKR99(ticket,
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
    );

    Mockito.verifyNoMoreInteractions(waybillsImportServiceSoap, objectMapper);
  }
}