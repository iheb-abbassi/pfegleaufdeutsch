package com.pflege.app;

import com.pflege.app.domain.dto.QuestionDtos;
import com.pflege.app.domain.entity.DifficultyLevel;
import com.pflege.app.domain.entity.Domain;
import com.pflege.app.domain.entity.Question;
import com.pflege.app.domain.entity.QuestionOption;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.entity.UserQuestionProgress;
import com.pflege.app.domain.repository.QuestionRepository;
import com.pflege.app.domain.repository.UserQuestionProgressRepository;
import com.pflege.app.domain.repository.UserRepository;
import com.pflege.app.domain.service.PracticeService;
import com.pflege.app.domain.service.QuestionMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserQuestionProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PracticeService practiceService;

    @Test
    void marksQuestionMasteredWhenSelectionExactlyMatchesCorrectAnswers() {
        Question question = question(10L, List.of(option(1L, true), option(2L, false), option(3L, true)));
        User user = new User();
        user.setEmail("user@test.local");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(progressRepository.findByUserIdAndQuestionId(5L, 10L)).thenReturn(Optional.empty());
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        QuestionDtos.AnswerResponse response = service().submitAnswer(5L, 10L, new QuestionDtos.AnswerRequest(List.of(1L, 3L)));

        ArgumentCaptor<UserQuestionProgress> captor = ArgumentCaptor.forClass(UserQuestionProgress.class);
        verify(progressRepository).save(captor.capture());
        assertThat(response.correct()).isTrue();
        assertThat(response.mastered()).isTrue();
        assertThat(captor.getValue().isMastered()).isTrue();
    }

    @Test
    void removesMasteryWhenSelectionIsIncorrect() {
        Question question = question(10L, List.of(option(1L, true), option(2L, false), option(3L, true)));
        UserQuestionProgress existing = new UserQuestionProgress();
        existing.setMastered(true);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(progressRepository.findByUserIdAndQuestionId(5L, 10L)).thenReturn(Optional.of(existing));

        QuestionDtos.AnswerResponse response = service().submitAnswer(5L, 10L, new QuestionDtos.AnswerRequest(List.of(1L)));

        assertThat(response.correct()).isFalse();
        assertThat(response.mastered()).isFalse();
        assertThat(existing.isMastered()).isFalse();
    }

    @Test
    void startSessionIncludesDomainNameAndOnlyUnmasteredQuestions() {
        Question mastered = question(10L, List.of(option(1L, true), option(2L, false), option(3L, true)));
        Question pending = question(11L, List.of(option(4L, true), option(5L, false), option(6L, false)));
        UserQuestionProgress progress = new UserQuestionProgress();
        progress.setQuestion(mastered);
        progress.setMastered(true);

        when(questionRepository.findAllActiveByDomainIdWithOptions(1L)).thenReturn(List.of(mastered, pending));
        when(progressRepository.findByUserId(5L)).thenReturn(List.of(progress));

        QuestionDtos.PracticeSessionResponse response = service().startSession(5L, 1L);

        assertThat(response.domainName()).isEqualTo("Grundpflege");
        assertThat(response.totalQuestions()).isEqualTo(2);
        assertThat(response.questions()).extracting(QuestionDtos.PracticeQuestionResponse::id).containsExactly(11L);
    }

    @Test
    void startSessionIncludesPreviouslyIncorrectQuestionsAgain() {
        Question incorrect = question(10L, List.of(option(1L, true), option(2L, false), option(3L, false)));
        UserQuestionProgress progress = new UserQuestionProgress();
        progress.setQuestion(incorrect);
        progress.setLastAnswerCorrect(false);
        progress.setMastered(false);

        when(questionRepository.findAllActiveByDomainIdWithOptions(1L)).thenReturn(List.of(incorrect));
        when(progressRepository.findByUserId(5L)).thenReturn(List.of(progress));

        QuestionDtos.PracticeSessionResponse response = service().startSession(5L, 1L);

        assertThat(response.questions()).extracting(QuestionDtos.PracticeQuestionResponse::id).containsExactly(10L);
    }

    private PracticeService service() {
        return new PracticeService(questionRepository, progressRepository, userRepository, new QuestionMapper());
    }

    private Question question(Long id, List<QuestionOption> options) {
        Question question = new Question();
        try {
            var field = Question.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(question, id);
        } catch (Exception ignored) {
        }
        Domain domain = new Domain();
        domain.setName("Grundpflege");
        question.setDomain(domain);
        question.setText("Frage");
        question.setDifficulty(DifficultyLevel.EASY);
        for (QuestionOption option : options) {
            option.setQuestion(question);
            question.getOptions().add(option);
        }
        return question;
    }

    private QuestionOption option(Long id, boolean correct) {
        QuestionOption option = new QuestionOption();
        try {
            var field = QuestionOption.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(option, id);
        } catch (Exception ignored) {
        }
        option.setText("Option " + id);
        option.setCorrect(correct);
        option.setDisplayOrder(id.intValue());
        return option;
    }
}
