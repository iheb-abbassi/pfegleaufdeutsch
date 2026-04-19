package com.pflege.app.auth;

import com.pflege.app.domain.entity.Role;

public record AuthenticatedUser(Long userId, String email, Role role) {
}
