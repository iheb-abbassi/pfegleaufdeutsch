package com.pflege.app;

import com.pflege.app.domain.entity.AuthProvider;
import com.pflege.app.domain.entity.Role;
import com.pflege.app.domain.entity.User;
import com.pflege.app.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:student-access;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class StudentAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registeredStudentCanUseLearnerEndpointsButNotAdminEndpoints() throws Exception {
        String token = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "student-access@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role", is("USER")))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"accessToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        User student = userRepository.findByEmailIgnoreCase("student-access@example.com").orElseThrow();
        assertThat(student.getRole()).isEqualTo(Role.USER);

        mockMvc.perform(get("/api/v1/domains")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/domains")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseAdminEndpoints() throws Exception {
        User admin = new User();
        admin.setEmail("admin-access@example.com");
        admin.setPasswordHash(passwordEncoder.encode("password123"));
        admin.setRole(Role.ADMIN);
        admin.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(admin);

        String token = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin-access@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role", is("ADMIN")))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"accessToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/v1/admin/domains")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
