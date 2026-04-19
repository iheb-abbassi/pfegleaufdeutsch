package com.pflege.app.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class QuestionDtos {

    public record QuestionOptionRequest(
            @NotBlank String text,
            boolean correct
    ) {
    }

    public record QuestionRequest(
            @NotNull Long domainId,
            @NotBlank String text,
            @NotBlank String difficulty,
            @NotEmpty @Size(min = 3, max = 3) List<@Valid QuestionOptionRequest> options
    ) {
    }

    public record QuestionOptionResponse(Long id, int displayOrder, String text, boolean correct) {
    }

    public record QuestionResponse(Long id, Long domainId, String domainName, String text, String difficulty, List<QuestionOptionResponse> options) {
    }

    public record PracticeQuestionResponse(Long id, String text, String difficulty, List<QuestionOptionResponse> options) {
    }

    public record PracticeSessionResponse(Long domainId, String domainName, List<PracticeQuestionResponse> questions) {
    }

    public record AnswerRequest(List<Long> selectedOptionIds) {
    }

    public record AnswerResponse(boolean correct, List<Long> correctOptionIds, boolean mastered) {
    }
}
