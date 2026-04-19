package com.pflege.app.common;

import java.time.Instant;

public record ApiErrorResponse(Instant timestamp, int status, String error, String message) {
}
