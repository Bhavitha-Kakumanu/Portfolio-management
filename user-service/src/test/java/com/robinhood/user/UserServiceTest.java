package com.robinhood.user;

import com.robinhood.user.dto.LoginRequest;
import com.robinhood.user.dto.RegisterRequest;
import com.robinhood.user.exception.UserAlreadyExistsException;
import com.robinhood.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

// @SpringBootTest starts the full Spring context (all beans, real DB via H2 in tests)
// @Transactional rolls back every test — DB is clean before each test method
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void register_createsUserAndReturnsToken() {
        var request = new RegisterRequest("johndoe", "john@example.com", "password123", "John", "Doe");

        var response = userService.register(request);

        assertThat(response.token()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.username()).isEqualTo("johndoe");
        assertThat(response.email()).isEqualTo("john@example.com");
    }

    @Test
    void register_throwsIfEmailAlreadyExists() {
        var request = new RegisterRequest("user1", "same@example.com", "pass1234", "A", "B");
        userService.register(request);

        var duplicate = new RegisterRequest("user2", "same@example.com", "pass1234", "C", "D");

        assertThatThrownBy(() -> userService.register(duplicate))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        userService.register(new RegisterRequest("loginuser", "login@test.com", "pass1234", "L", "U"));

        var response = userService.login(new LoginRequest("login@test.com", "pass1234"));

        assertThat(response.token()).isNotBlank();
    }

    @Test
    void login_throwsForWrongPassword() {
        userService.register(new RegisterRequest("wrongpass", "wp@test.com", "correctpass", "W", "P"));

        assertThatThrownBy(() -> userService.login(new LoginRequest("wp@test.com", "wrongpass")))
            .isInstanceOf(BadCredentialsException.class);
    }
}
