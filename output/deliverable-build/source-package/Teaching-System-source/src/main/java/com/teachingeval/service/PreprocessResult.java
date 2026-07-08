package com.teachingeval.service;

public class PreprocessResult {

    public static final String STATUS_SKIPPED = "SKIPPED";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private final String status;
    private final String message;
    private final String rawResponse;

    private PreprocessResult(String status, String message, String rawResponse) {
        this.status = status;
        this.message = message;
        this.rawResponse = rawResponse;
    }

    public static PreprocessResult skipped(String message) {
        return new PreprocessResult(STATUS_SKIPPED, message, null);
    }

    public static PreprocessResult success(String rawResponse) {
        return new PreprocessResult(STATUS_SUCCESS, "Py预处理完成", rawResponse);
    }

    public static PreprocessResult failed(String message, String rawResponse) {
        return new PreprocessResult(STATUS_FAILED, message, rawResponse);
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}
