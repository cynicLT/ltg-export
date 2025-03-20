package org.cynic.ltg_export.domain.holder;

import org.springframework.context.ApplicationContext;

public final class ApplicationContextHolder {

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<?> clazz) {
        return (T) applicationContext.getBean(clazz);
    }
}
