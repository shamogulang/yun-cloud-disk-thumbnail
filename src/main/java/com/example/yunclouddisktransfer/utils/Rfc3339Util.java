package com.example.yunclouddisktransfer.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Rfc3339Util {

    public static String toRfc3339String(Date date) {
        if (date == null) {
            return null;
        }
        Instant instant = date.toInstant();
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
