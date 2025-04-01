package org.cynic.ltg_export.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.cynic.ltg_export.Configuration.ExportConfiguration;
import org.cynic.ltg_export.Configuration.ExportConfiguration.ReportExportConfiguration.Type;
import org.cynic.ltg_export.domain.ApplicationException;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ExportServiceTest {

  private ExportService exportService;

  @Mock
  private CSVPrinter printer;

  private final String nameKR99 = Instancio.of(String.class).withSeed(1L).create();
  private final String nameUnknown = Instancio.of(String.class).withSeed(2L).create();
  private final Set<ExportConfiguration.ReportExportConfiguration> configurations = Set.of(
    Instancio.of(ExportConfiguration.ReportExportConfiguration.class)
      .set(Select.field("name"), nameKR99)
      .set(Select.field("type"), Type.KR99)
      .create()
  );

  @BeforeEach
  void setUp() {
    this.exportService = new ExportService(utputStream -> printer, (expression, value) -> value, configurations);
  }

  @Test
  void exportWhenOk() throws IOException {
    JsonNode item = Instancio.create(ObjectNode.class);

    Assertions.assertThat(exportService.export(nameKR99, List.of(item)))
      .isNotNull();

    Mockito.verify(printer, Mockito.times(2)).printRecord(Mockito.<List<String>>any());

    Mockito.verify(printer).close();
    Mockito.verifyNoMoreInteractions(printer);
  }

  @Test
  void exportWhenErrorExport() throws IOException {
    JsonNode item = Instancio.create(ObjectNode.class);
    IOException cause = Instancio.create(IOException.class);

    Mockito.doThrow(cause).when(printer).printRecord(Mockito.<List<String>>any());

    Assertions.assertThatThrownBy(() -> exportService.export(nameKR99, List.of(item)))
      .asInstanceOf(InstanceOfAssertFactories.throwable(ApplicationException.class))
      .matches(it -> StringUtils.equals(it.getCode(), "error.export"))
      .extracting(ApplicationException::getValues)
      .asInstanceOf(InstanceOfAssertFactories.MAP)
      .containsOnly(Map.entry("message", ExceptionUtils.getRootCauseMessage(cause)));

    Mockito.verify(printer).close();

    Mockito.verifyNoMoreInteractions(printer);
  }

  @Test
  void exportWhenErrorNotSupported() {
    JsonNode item = Instancio.create(ObjectNode.class);

    Assertions.assertThatThrownBy(() -> exportService.export(nameUnknown, List.of(item)))
      .asInstanceOf(InstanceOfAssertFactories.throwable(ApplicationException.class))
      .matches(it -> StringUtils.equals(it.getCode(), "error.export.not-supported"))
      .extracting(ApplicationException::getValues)
      .asInstanceOf(InstanceOfAssertFactories.MAP)
      .containsOnly(Map.entry("name", nameUnknown));

    Mockito.verifyNoMoreInteractions(printer);
  }
}