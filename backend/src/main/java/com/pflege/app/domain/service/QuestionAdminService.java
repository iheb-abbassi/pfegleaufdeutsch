package com.pflege.app.domain.service;

import com.pflege.app.common.ApiException;
import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.entity.DifficultyLevel;
import com.pflege.app.domain.entity.Domain;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.QuestionOption;
import com.pflege.app.domain.repository.DomainRepository;
import com.pflege.app.domain.repository.QuestionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuestionAdminService {

    private final DomainRepository domainRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper mapper;

    public QuestionAdminService(DomainRepository domainRepository, QuestionRepository questionRepository, QuestionMapper mapper) {
        this.domainRepository = domainRepository;
        this.questionRepository = questionRepository;
        this.mapper = mapper;
    }

    public List<QuestionDtos.QuestionResponse> list() {
        return questionRepository.findAllActiveWithOptions().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public QuestionDtos.QuestionResponse create(QuestionDtos.QuestionRequest request) {
        validateQuestion(request);
        Question question = new Question();
        apply(question, request);
        questionRepository.save(question);
        return mapper.toResponse(question);
    }

    @Transactional
    public QuestionDtos.QuestionResponse update(Long id, QuestionDtos.QuestionRequest request) {
        validateQuestion(request);
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        question.getOptions().clear();
        apply(question, request);
        return mapper.toResponse(question);
    }

    @Transactional
    public void delete(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        question.setActive(false);
    }

    private void apply(Question question, QuestionDtos.QuestionRequest request) {
        Domain domain = domainRepository.findById(request.domainId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Domain not found"));
        question.setDomain(domain);
        question.setText(request.text());
        question.setDifficulty(DifficultyLevel.valueOf(request.difficulty().toUpperCase()));
        int index = 0;
        for (QuestionDtos.QuestionOptionRequest optionRequest : request.options()) {
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setDisplayOrder(index++);
            option.setText(optionRequest.text());
            option.setCorrect(optionRequest.correct());
            question.getOptions().add(option);
        }
    }

    private void validateQuestion(QuestionDtos.QuestionRequest request) {
        long correctCount = request.options().stream().filter(QuestionDtos.QuestionOptionRequest::correct).count();
        if (correctCount == 0 || correctCount > 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Questions must have between 1 and 3 correct answers");
        }
    }
}
