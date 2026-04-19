package com.pflege.app.domain.dto;

import java.time.Instant;
import java.util.List;

public class ExamDtos {

    public record ExamUnlockResponse(boolean unlocked, long masteredQuestions, long totalQuestions) {
    }

    public record ExamQuestionResponse(Long examQuestionId, Long questionId, String text, String difficulty, List<QuestionDtos.QuestionOptionResponse> options, List<Long> selectedOptionIds) {
    }

    public record ExamAttemptResponse(Long attemptId, boolean submitted, int score, int totalQuestions, boolean passed, Instant startedAt, Instant completedAt, List<ExamQuestionResponse> questions) {
    }

    public record SaveExamAnswerRequest(List<Long> selectedOptionIds) {
    }

    public record ExamHistoryItemResponse(Long attemptId, int score, int totalQuestions, boolean passed, Instant startedAt, Instant completedAt, int correctAnswers, int incorrectAnswers) {
    }
}
