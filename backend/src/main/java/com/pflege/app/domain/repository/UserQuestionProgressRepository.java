package com.pflege.app.domain.repository;

import com.pflege.app.domain.entity.UserQuestionProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserQuestionProgressRepository extends JpaRepository<UserQuestionProgress, Long> {
    Optional<UserQuestionProgress> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<UserQuestionProgress> findByUserId(Long userId);

    @Query("select p from UserQuestionProgress p join fetch p.question q join fetch q.domain where p.user.id = :userId")
    List<UserQuestionProgress> findByUserIdWithQuestionAndDomain(Long userId);

    @Query("select count(p) from UserQuestionProgress p where p.user.id = :userId and p.mastered = true")
    long countMasteredByUserId(Long userId);
}
