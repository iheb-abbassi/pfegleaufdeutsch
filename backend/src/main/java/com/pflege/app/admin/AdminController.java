package com.pflege.app.admin;

import com.pflege.app.domain.dto.DomainDtos;
import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.service.DomainService;
import com.pflege.app.domain.service.QuestionAdminService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final DomainService domainService;
    private final QuestionAdminService questionAdminService;
    private final ImportService importService;

    public AdminController(DomainService domainService, QuestionAdminService questionAdminService, ImportService importService) {
        this.domainService = domainService;
        this.questionAdminService = questionAdminService;
        this.importService = importService;
    }

    @GetMapping("/domains")
    public List<DomainDtos.DomainResponse> listDomains() {
        return domainService.listAdmin();
    }

    @PostMapping("/domains")
    public DomainDtos.DomainResponse createDomain(@RequestBody DomainDtos.DomainRequest request) {
        return domainService.create(request);
    }

    @PutMapping("/domains/{id}")
    public DomainDtos.DomainResponse updateDomain(@PathVariable Long id, @RequestBody DomainDtos.DomainRequest request) {
        return domainService.update(id, request);
    }

    @GetMapping("/questions")
    public List<QuestionDtos.QuestionResponse> listQuestions() {
        return questionAdminService.list();
    }

    @PostMapping("/questions")
    public QuestionDtos.QuestionResponse createQuestion(@Valid @RequestBody QuestionDtos.QuestionRequest request) {
        return questionAdminService.create(request);
    }

    @PutMapping("/questions/{id}")
    public QuestionDtos.QuestionResponse updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDtos.QuestionRequest request) {
        return questionAdminService.update(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionAdminService.delete(id);
    }

    @PostMapping("/import/questions/csv")
    public ImportDtos.ImportResponse importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importCsv(file);
    }

    @PostMapping("/import/questions/excel")
    public ImportDtos.ImportResponse importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importExcel(file);
    }
}
