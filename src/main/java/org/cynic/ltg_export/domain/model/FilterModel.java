package org.cynic.ltg_export.domain.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record FilterModel(
    String paymentPeriod,
    LocalDate changeDateFrom,
    LocalDate changeDateTo,
    String systemNumber,
    LocalDate sendDate,
    LocalDate departureDateFrom,
    LocalDate departureDateTo,
    LocalDate arrivalDateFrom,
    LocalDate arrivalDateTo,
    String number
) implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

}
