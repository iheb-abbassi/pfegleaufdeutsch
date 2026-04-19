package com.pflege.app.admin;

import java.util.List;

public class ImportDtos {

    public record ImportError(int row, String message) {
    }

    public record ImportResponse(int importedCount, List<ImportError> errors) {
    }
}
