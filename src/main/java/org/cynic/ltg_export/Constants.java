package org.cynic.ltg_export;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Locale;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

public final class Constants {


  private Constants() {
  }


  public static final Marker AUDIT_MARKER = MarkerFactory.getMarker("AUDIT");
  public static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();
  public static final Locale LOCALE = Locale.forLanguageTag("lt");
  public static final String VALUE = "value";

  public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS = new DateTimeFormatterBuilder()
    .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .appendLiteral('_')
    .appendValue(MONTH_OF_YEAR, 2)
    .appendLiteral('_')
    .appendValue(DAY_OF_MONTH, 2)
    .appendLiteral('_')
    .appendValue(HOUR_OF_DAY, 2)
    .appendLiteral('_')
    .appendValue(MINUTE_OF_HOUR, 2)
    .appendLiteral('_')
    .appendValue(SECOND_OF_MINUTE, 2)
    .toFormatter(LOCALE);

  public enum Template {
    JXML_PATH("/jxml/%s.fxml"),
    EXPRESSION("""
      import %s;
      import %s;
      
      %s
      """);

    private final String template;

    Template(String template) {
      this.template = template;
    }

    public String template(Object... values) {
      return String.format(LOCALE, template, values);
    }
  }
}
