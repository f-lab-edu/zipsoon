package com.zipsoon.api.infrastructure.logging.filter;


import org.slf4j.MDC;

public class RequestIdUtil {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    public static String getRequestId() {
        return MDC.get(REQUEST_ID_HEADER);
    }
}