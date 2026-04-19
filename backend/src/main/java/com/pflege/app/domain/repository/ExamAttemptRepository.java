package com.pflege.app.domain.repository;

import com.pflege.app.domain.entity.ExamAttempt;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    @EntityGraph(attributePaths = {"questions", "questions.question", "questions.question.options"})
    Optional<ExamAttempt> findById(Long id);

    List<ExamAttempt> findByUserIdOrderByStartedAtDesc(Long userId);
}
