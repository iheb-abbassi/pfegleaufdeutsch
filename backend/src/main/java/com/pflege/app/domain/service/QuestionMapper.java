package com.pflege.app.domain.service;

import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.QuestionOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {

    public QuestionDtos.QuestionResponse toResponse(Question question) {
        return new QuestionDtos.QuestionResponse(
                question.getId(),
                question.getDomain().getId(),
                question.getDomain().getName(),
                question.getText(),
                question.getDifficulty().name(),
                question.getOptions().stream().map(this::toOptionResponse).toList()
        );
    }

    public QuestionDtos.PracticeQuestionResponse toPracticeResponse(Question question) {
        List<QuestionOption> shuffled = question.getOptions().stream().collect(Collectors.toList());
        Collections.shuffle(shuffled);
        return new QuestionDtos.PracticeQuestionResponse(
                question.getId(),
                question.getText(),
                question.getDifficulty().name(),
                shuffled.stream().map(this::toOptionResponse).toList()
        );
    }

    public QuestionDtos.QuestionOptionResponse toOptionResponse(QuestionOption option) {
        return new QuestionDtos.QuestionOptionResponse(option.getId(), option.getDisplayOrder(), option.getText(), option.isCorrect());
    }
}
