package com.pflege.app.domain.repository;

import com.pflege.app.domain.entity.Role;
import com.pflege.app.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByRole(Role role);
}
