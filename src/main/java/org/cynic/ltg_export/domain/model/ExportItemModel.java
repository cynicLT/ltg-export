package org.cynic.ltg_export.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;

public record ExportItemModel(
    String label,
    Integer index,
    Function<JsonNode, String> extractor
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
