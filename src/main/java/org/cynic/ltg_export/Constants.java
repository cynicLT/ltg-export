package org.cynic.ltg_export;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.ZoneId;

public final class Constants {

    private Constants() {
    }

    public static final Marker AUDIT_MARKER = MarkerFactory.getMarker("AUDIT");
    public static final ZoneId SYSTEM_ZONE_ID = ZoneId.systemDefault();


}
