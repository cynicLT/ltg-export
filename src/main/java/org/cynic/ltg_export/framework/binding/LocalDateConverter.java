package org.cynic.ltg_export.framework.binding;

import java.time.LocalDate;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public final class LocalDateConverter {

    public static LocalDate parse(String value) {

        return Optional.ofNullable(value)
            .map(StringUtils::trimToNull)
            .map(LocalDate::parse)
            .orElse(null);
    }

    public static String format(LocalDate value) {
        return Optional.ofNullable(value)
            .map(LocalDate::toString)
            .map(StringUtils::trimToNull)
            .orElse(null);
    }
}
