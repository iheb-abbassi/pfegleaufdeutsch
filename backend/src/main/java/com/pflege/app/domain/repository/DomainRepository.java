package com.pflege.app.domain.repository;

import com.pflege.app.domain.entity.Domain;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findBySlug(String slug);
}
