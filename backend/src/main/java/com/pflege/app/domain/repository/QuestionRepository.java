package com.pflege.app.domain.repository;

import com.pflege.app.domain.entity.DifficultyLevel;
import com.pflege.app.domain.entity.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("select distinct q from Question q join fetch q.domain left join fetch q.options where q.active = true")
    List<Question> findAllActiveWithOptions();

    @Query("select distinct q from Question q left join fetch q.options where q.domain.id = :domainId and q.active = true")
    List<Question> findAllActiveByDomainIdWithOptions(Long domainId);

    @Query("select distinct q from Question q left join fetch q.options where q.difficulty = :difficulty and q.active = true")
    List<Question> findAllActiveByDifficultyWithOptions(DifficultyLevel difficulty);
}
