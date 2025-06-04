package com.pianomastr64.usermanagement.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class ErrorResponseUtil {
    private ErrorResponseUtil() {}
    
    public static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
