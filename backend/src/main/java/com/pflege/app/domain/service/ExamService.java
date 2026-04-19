package com.pflege.app.domain.service;

import com.pflege.app.common.ApiException;
import com.pflege.app.domain.dto.ExamDtos;
import com.pflege.app.domain.entity.DifficultyLevel;
import com.pflege.app.domain.entity.ExamAttempt;
import com.pflege.app.domain.entity.ExamAttemptQuestion;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.QuestionOption;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.entity.UserQuestionProgress;
import com.pflege.app.domain.repository.ExamAttemptRepository;
import com.pflege.app.domain.repository.QuestionRepository;
import com.pflege.app.domain.repository.UserQuestionProgressRepository;
import com.pflege.app.domain.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExamService {

    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;
    private final UserQuestionProgressRepository progressRepository;
    private final QuestionMapper mapper;

    public ExamService(QuestionRepository questionRepository, ExamAttemptRepository examAttemptRepository, UserRepository userRepository, UserQuestionProgressRepository progressRepository, QuestionMapper mapper) {
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.mapper = mapper;
    }

    public ExamDtos.ExamUnlockResponse getUnlockStatus(Long userId) {
        long total = questionRepository.findAllActiveWithOptions().size();
        long mastered = progressRepository.countMasteredByUserId(userId);
        return new ExamDtos.ExamUnlockResponse(total > 0 && mastered >= total, mastered, total);
    }

    @Transactional
    public ExamDtos.ExamAttemptResponse createExam(Long userId) {
        ExamDtos.ExamUnlockResponse unlock = getUnlockStatus(userId);
        if (!unlock.unlocked()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Exam is not unlocked");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        List<Question> selected = new ArrayList<>();
        selected.addAll(take(DifficultyLevel.EASY, 5));
        selected.addAll(take(DifficultyLevel.MEDIUM, 15));
        selected.addAll(take(DifficultyLevel.HARD, 10));
        if (selected.size() < 30) {
            Set<Long> chosenIds = selected.stream().map(Question::getId).collect(Collectors.toSet());
            questionRepository.findAllActiveWithOptions().stream()
                    .filter(question -> !chosenIds.contains(question.getId()))
                    .limit(30L - selected.size())
                    .forEach(selected::add);
        }
        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setStartedAt(Instant.now());
        attempt.setTotalQuestions(selected.size());
        attempt.setScore(0);
        attempt.setPassed(false);
        attempt.setSubmitted(false);
        for (Question question : selected) {
            ExamAttemptQuestion examQuestion = new ExamAttemptQuestion();
            examQuestion.setExamAttempt(attempt);
            examQuestion.setQuestion(question);
            examQuestion.setCorrectOptionIds(question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .map(option -> option.getId().toString())
                    .sorted()
                    .collect(Collectors.joining(",")));
            examQuestion.setSelectedOptionIds("");
            examQuestion.setCorrect(false);
            examQuestion.setDifficulty(question.getDifficulty());
            attempt.getQuestions().add(examQuestion);
        }
        examAttemptRepository.save(attempt);
        return toAttemptResponse(attempt);
    }

    public ExamDtos.ExamAttemptResponse getAttempt(Long userId, Long attemptId) {
        return toAttemptResponse(getOwnedAttempt(userId, attemptId));
    }

    @Transactional
    public ExamDtos.ExamAttemptResponse saveAnswer(Long userId, Long attemptId, Long questionId, ExamDtos.SaveExamAnswerRequest request) {
        ExamAttempt attempt = getOwnedAttempt(userId, attemptId);
        ExamAttemptQuestion examQuestion = attempt.getQuestions().stream()
                .filter(item -> item.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not part of exam"));
        List<Long> selected = request.selectedOptionIds() == null ? List.of() : request.selectedOptionIds();
        examQuestion.setSelectedOptionIds(selected.stream().map(String::valueOf).sorted().collect(Collectors.joining(",")));
        return toAttemptResponse(attempt);
    }

    @Transactional
    public ExamDtos.ExamAttemptResponse submit(Long userId, Long attemptId) {
        ExamAttempt attempt = getOwnedAttempt(userId, attemptId);
        int score = 0;
        for (ExamAttemptQuestion examQuestion : attempt.getQuestions()) {
            boolean correct = examQuestion.getSelectedOptionIds().equals(examQuestion.getCorrectOptionIds());
            examQuestion.setCorrect(correct);
            if (correct) {
                score++;
            } else {
                UserQuestionProgress progress = progressRepository.findByUserIdAndQuestionId(userId, examQuestion.getQuestion().getId())
                        .orElseGet(() -> {
                            UserQuestionProgress created = new UserQuestionProgress();
                            created.setUser(attempt.getUser());
                            created.setQuestion(examQuestion.getQuestion());
                            return created;
                        });
                progress.setAnsweredAt(Instant.now());
                progress.setLastAnswerCorrect(false);
                progress.setMastered(false);
                progressRepository.save(progress);
            }
        }
        attempt.setScore(score);
        attempt.setSubmitted(true);
        attempt.setCompletedAt(Instant.now());
        attempt.setPassed(score >= 24);
        return toAttemptResponse(attempt);
    }

    public List<ExamDtos.ExamHistoryItemResponse> history(Long userId) {
        return examAttemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(attempt -> new ExamDtos.ExamHistoryItemResponse(
                        attempt.getId(),
                        attempt.getScore(),
                        attempt.getTotalQuestions(),
                        attempt.isPassed(),
                        attempt.getStartedAt(),
                        attempt.getCompletedAt(),
                        (int) attempt.getQuestions().stream().filter(ExamAttemptQuestion::isCorrect).count(),
                        (int) attempt.getQuestions().stream().filter(question -> !question.isCorrect()).count()
                ))
                .toList();
    }

    private List<Question> take(DifficultyLevel difficulty, int count) {
        return questionRepository.findAllActiveByDifficultyWithOptions(difficulty).stream()
                .sorted(Comparator.comparing(Question::getId))
                .limit(count)
                .toList();
    }

    private ExamAttempt getOwnedAttempt(Long userId, Long attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Exam not found"));
        if (!attempt.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return attempt;
    }

    private ExamDtos.ExamAttemptResponse toAttemptResponse(ExamAttempt attempt) {
        return new ExamDtos.ExamAttemptResponse(
                attempt.getId(),
                attempt.isSubmitted(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                attempt.isPassed(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getQuestions().stream().map(question -> new ExamDtos.ExamQuestionResponse(
                        question.getId(),
                        question.getQuestion().getId(),
                        question.getQuestion().getText(),
                        question.getDifficulty().name(),
                        question.getQuestion().getOptions().stream().map(mapper::toOptionResponse).toList(),
                        parseSelected(question.getSelectedOptionIds())
                )).toList()
        );
    }

    private List<Long> parseSelected(String selected) {
        if (selected == null || selected.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(selected.split(",")).map(Long::valueOf).toList();
    }
}
