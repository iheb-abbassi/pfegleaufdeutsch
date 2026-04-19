package com.pflege.app.domain.service;

import com.pflege.app.common.ApiException;
import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.QuestionOption;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.entity.UserQuestionProgress;
import com.pflege.app.domain.repository.QuestionRepository;
import com.pflege.app.domain.repository.UserQuestionProgressRepository;
import com.pflege.app.domain.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticeService {

    private final QuestionRepository questionRepository;
    private final UserQuestionProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final QuestionMapper mapper;

    public PracticeService(QuestionRepository questionRepository, UserQuestionProgressRepository progressRepository, UserRepository userRepository, QuestionMapper mapper) {
        this.questionRepository = questionRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public QuestionDtos.PracticeSessionResponse startSession(Long userId, Long domainId) {
        List<Question> questions = questionRepository.findAllActiveByDomainIdWithOptions(domainId);
        Set<Long> masteredIds = progressRepository.findByUserId(userId).stream()
                .filter(UserQuestionProgress::isMastered)
                .map(progress -> progress.getQuestion().getId())
                .collect(Collectors.toSet());
        List<QuestionDtos.PracticeQuestionResponse> pending = questions.stream()
                .filter(question -> !masteredIds.contains(question.getId()))
                .map(mapper::toPracticeResponse)
                .toList();
        String domainName = questions.isEmpty() ? "" : questions.get(0).getDomain().getName();
        return new QuestionDtos.PracticeSessionResponse(domainId, domainName, pending);
    }

    @Transactional
    public QuestionDtos.AnswerResponse submitAnswer(Long userId, Long questionId, QuestionDtos.AnswerRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        Set<Long> selected = request.selectedOptionIds() == null ? Set.of() : Set.copyOf(request.selectedOptionIds());
        Set<Long> correct = question.getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getId)
                .collect(Collectors.toSet());
        boolean isCorrect = selected.equals(correct);
        UserQuestionProgress progress = progressRepository.findByUserIdAndQuestionId(userId, questionId)
                .orElseGet(() -> {
                    UserQuestionProgress newProgress = new UserQuestionProgress();
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
                    newProgress.setUser(user);
                    newProgress.setQuestion(question);
                    return newProgress;
                });
        progress.setLastAnswerCorrect(isCorrect);
        progress.setMastered(isCorrect);
        progress.setAnsweredAt(Instant.now());
        progressRepository.save(progress);
        return new QuestionDtos.AnswerResponse(isCorrect, correct.stream().toList(), progress.isMastered());
    }
}
