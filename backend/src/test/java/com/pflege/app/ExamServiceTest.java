package com.pflege.app;

import com.pflege.app.domain.dto.ExamDtos;
import com.pflege.app.domain.entity.DifficultyLevel;
import com.pflege.app.domain.entity.Domain;
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
import com.pflege.app.domain.service.ExamService;
import com.pflege.app.domain.service.QuestionMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Test
    void unlocksExamOnlyWhenAllQuestionsAreMastered() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        ExamAttemptRepository examAttemptRepository = mock(ExamAttemptRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserQuestionProgressRepository progressRepository = mock(UserQuestionProgressRepository.class);
        ExamService service = new ExamService(questionRepository, examAttemptRepository, userRepository, progressRepository, new QuestionMapper());

        when(questionRepository.findAllActiveWithOptions()).thenReturn(List.of(question(1L), question(2L), question(3L)));
        when(progressRepository.countMasteredByUserId(9L)).thenReturn(2L, 3L);

        ExamDtos.ExamUnlockResponse locked = service.getUnlockStatus(9L);
        ExamDtos.ExamUnlockResponse unlocked = service.getUnlockStatus(9L);

        assertThat(locked.unlocked()).isFalse();
        assertThat(unlocked.unlocked()).isTrue();
    }

    @Test
    void submittingExamMarksIncorrectQuestionsAsNotMastered() {
        QuestionRepository questionRepository = mock(QuestionRepository.class);
        ExamAttemptRepository examAttemptRepository = mock(ExamAttemptRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UserQuestionProgressRepository progressRepository = mock(UserQuestionProgressRepository.class);
        ExamService service = new ExamService(questionRepository, examAttemptRepository, userRepository, progressRepository, new QuestionMapper());

        User user = new User();
        user.setEmail("user@test.local");
        setId(user, 7L);

        Question question = question(11L);
        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setStartedAt(Instant.now());
        attempt.setTotalQuestions(1);

        ExamAttemptQuestion examQuestion = new ExamAttemptQuestion();
        examQuestion.setExamAttempt(attempt);
        examQuestion.setQuestion(question);
        examQuestion.setCorrectOptionIds("1,3");
        examQuestion.setSelectedOptionIds("1");
        examQuestion.setDifficulty(DifficultyLevel.EASY);
        attempt.getQuestions().add(examQuestion);

        when(examAttemptRepository.findById(5L)).thenReturn(Optional.of(attempt));
        when(progressRepository.findByUserIdAndQuestionId(7L, 11L)).thenReturn(Optional.of(new UserQuestionProgress()));

        ExamDtos.ExamAttemptResponse response = service.submit(7L, 5L);

        ArgumentCaptor<UserQuestionProgress> captor = ArgumentCaptor.forClass(UserQuestionProgress.class);
        verify(progressRepository).save(captor.capture());
        assertThat(response.score()).isZero();
        assertThat(response.passed()).isFalse();
        assertThat(captor.getValue().isMastered()).isFalse();
    }

    private Question question(Long id) {
        Question question = new Question();
        setId(question, id);
        question.setText("Frage " + id);
        question.setDifficulty(DifficultyLevel.EASY);
        Domain domain = new Domain();
        domain.setName("Domain");
        question.setDomain(domain);
        question.getOptions().addAll(new ArrayList<>(List.of(option(1L, true), option(2L, false), option(3L, true))));
        question.getOptions().forEach(option -> option.setQuestion(question));
        return question;
    }

    private QuestionOption option(Long id, boolean correct) {
        QuestionOption option = new QuestionOption();
        setId(option, id);
        option.setText("Option " + id);
        option.setCorrect(correct);
        option.setDisplayOrder(id.intValue());
        return option;
    }

    private void setId(Object target, Long id) {
        try {
            var field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception ignored) {
        }
    }
}
