package org.cynic.ltg_export.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.cynic.ltg_export.domain.ApplicationException;

public final class AlertUtils {

    private AlertUtils() {
    }

    public static void error(ApplicationException exception) {
        alert(
            "Error",
            exception.getCode(),
            exception.getValues().toString()
        );

    }

    public static void error(Throwable exception) {
        alert(
            "Unknown error",
            ExceptionUtils.getRootCauseMessage(exception),
            ExceptionUtils.getStackTrace(exception)
        );
    }


    public static void info(String title, String content) {
        alert(
            title,
            null,
            content
        );
    }

    private static void alert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(StringUtils.abbreviate(header, "...", 100));
        alert.setContentText(StringUtils.abbreviate(content, "...", 400));
        alert.getButtonTypes().setAll(ButtonType.CLOSE);

        alert.showAndWait();
    }

}
