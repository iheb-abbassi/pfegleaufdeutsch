package com.pflege.app.admin;

import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.service.QuestionAdminService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportService {

    private final QuestionAdminService questionAdminService;

    public ImportService(QuestionAdminService questionAdminService) {
        this.questionAdminService = questionAdminService;
    }

    public ImportDtos.ImportResponse importCsv(MultipartFile file) throws IOException {
        List<ImportDtos.ImportError> errors = new ArrayList<>();
        int imported = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(reader)) {
            int rowNum = 1;
            for (var record : parser) {
                rowNum++;
                try {
                    questionAdminService.create(toRequest(
                            record.get("domainId"),
                            record.get("text"),
                            record.get("difficulty"),
                            record.get("option1"),
                            record.get("option2"),
                            record.get("option3"),
                            record.get("correct1"),
                            record.get("correct2"),
                            record.get("correct3")
                    ));
                    imported++;
                } catch (Exception ex) {
                    errors.add(new ImportDtos.ImportError(rowNum, ex.getMessage()));
                }
            }
        }
        return new ImportDtos.ImportResponse(imported, errors);
    }

    public ImportDtos.ImportResponse importExcel(MultipartFile file) throws IOException {
        List<ImportDtos.ImportError> errors = new ArrayList<>();
        int imported = 0;
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            var sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    questionAdminService.create(toRequest(
                            get(row, 0),
                            get(row, 1),
                            get(row, 2),
                            get(row, 3),
                            get(row, 4),
                            get(row, 5),
                            get(row, 6),
                            get(row, 7),
                            get(row, 8)
                    ));
                    imported++;
                } catch (Exception ex) {
                    errors.add(new ImportDtos.ImportError(i + 1, ex.getMessage()));
                }
            }
        }
        return new ImportDtos.ImportResponse(imported, errors);
    }

    private String get(Row row, int cellIndex) {
        var cell = row.getCell(cellIndex);
        return cell == null ? "" : cell.toString();
    }

    private QuestionDtos.QuestionRequest toRequest(
            String domainId,
            String text,
            String difficulty,
            String option1,
            String option2,
            String option3,
            String correct1,
            String correct2,
            String correct3
    ) {
        return new QuestionDtos.QuestionRequest(
                Long.valueOf(domainId),
                text,
                difficulty,
                List.of(
                        new QuestionDtos.QuestionOptionRequest(option1, Boolean.parseBoolean(correct1)),
                        new QuestionDtos.QuestionOptionRequest(option2, Boolean.parseBoolean(correct2)),
                        new QuestionDtos.QuestionOptionRequest(option3, Boolean.parseBoolean(correct3))
                )
        );
    }
}
