package com.pflege.app.learner;

import com.pflege.app.auth.AuthenticatedUser;
import com.pflege.app.domain.dto.DomainDtos;
import com.pflege.app.domain.dto.ExamDtos;
import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.service.DomainService;
import com.pflege.app.domain.service.ExamService;
import com.pflege.app.domain.service.PracticeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class LearnerController {

    private final DomainService domainService;
    private final PracticeService practiceService;
    private final ExamService examService;

    public LearnerController(DomainService domainService, PracticeService practiceService, ExamService examService) {
        this.domainService = domainService;
        this.practiceService = practiceService;
        this.examService = examService;
    }

    @GetMapping("/domains")
    public List<DomainDtos.DomainResponse> domains(@AuthenticationPrincipal AuthenticatedUser user) {
        return domainService.listForUser(user.userId());
    }

    @GetMapping("/practice/domains/{domainId}/session")
    public QuestionDtos.PracticeSessionResponse session(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long domainId) {
        return practiceService.startSession(user.userId(), domainId);
    }

    @PostMapping("/practice/questions/{questionId}/answer")
    public QuestionDtos.AnswerResponse submitAnswer(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long questionId,
                                                    @Valid @RequestBody QuestionDtos.AnswerRequest request) {
        return practiceService.submitAnswer(user.userId(), questionId, request);
    }

    @GetMapping("/exams/unlock-status")
    public ExamDtos.ExamUnlockResponse unlockStatus(@AuthenticationPrincipal AuthenticatedUser user) {
        return examService.getUnlockStatus(user.userId());
    }

    @PostMapping("/exams")
    public ExamDtos.ExamAttemptResponse createExam(@AuthenticationPrincipal AuthenticatedUser user) {
        return examService.createExam(user.userId());
    }

    @GetMapping("/exams/{attemptId}")
    public ExamDtos.ExamAttemptResponse getExam(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long attemptId) {
        return examService.getAttempt(user.userId(), attemptId);
    }

    @PostMapping("/exams/{attemptId}/questions/{questionId}/answer")
    public ExamDtos.ExamAttemptResponse saveExamAnswer(@AuthenticationPrincipal AuthenticatedUser user,
                                                       @PathVariable Long attemptId,
                                                       @PathVariable Long questionId,
                                                       @RequestBody ExamDtos.SaveExamAnswerRequest request) {
        return examService.saveAnswer(user.userId(), attemptId, questionId, request);
    }

    @PostMapping("/exams/{attemptId}/submit")
    public ExamDtos.ExamAttemptResponse submit(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long attemptId) {
        return examService.submit(user.userId(), attemptId);
    }

    @GetMapping("/exams/history")
    public List<ExamDtos.ExamHistoryItemResponse> history(@AuthenticationPrincipal AuthenticatedUser user) {
        return examService.history(user.userId());
    }
}
