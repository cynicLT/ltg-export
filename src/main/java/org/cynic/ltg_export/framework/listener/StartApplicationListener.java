package org.cynic.ltg_export.framework.listener;

import com.sun.javafx.application.LauncherImpl;
import java.util.concurrent.Executors;
import org.cynic.ltg_export.domain.holder.ApplicationContextHolder;
import org.cynic.ltg_export.ui.ApplicationForm;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

public class StartApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContextHolder.setApplicationContext(event.getApplicationContext());

        Executors.newSingleThreadExecutor().submit(() -> LauncherImpl.launchApplication(ApplicationForm.class, null));
    }
}
