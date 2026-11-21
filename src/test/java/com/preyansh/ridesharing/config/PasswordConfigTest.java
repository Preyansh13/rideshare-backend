package com.preyansh.ridesharing.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordConfigTest {

    private final PasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    @Test
    void encodePassword_shouldReturnHashedPassword() {

        // Arrange
        String rawPassword = "RideShare@123";

        // Act
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Assert
        assertThat(hashedPassword).isNotEqualTo(rawPassword);
        assertThat(hashedPassword).startsWith("$2");
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {

        // Arrange
        String rawPassword = "RideShare@123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(rawPassword, hashedPassword);

        // Assert
        assertThat(matches).isTrue();
    }

    @Test
    void matches_shouldReturnFalseForIncorrectPassword() {

        // Arrange
        String rawPassword = "RideShare@123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Act
        boolean matches = passwordEncoder.matches(
                "WrongPassword@123",
                hashedPassword
        );

        // Assert
        assertThat(matches).isFalse();
    }

    @Test
    void encodePassword_shouldGenerateDifferentHashesForSamePassword() {

        // Arrange
        String rawPassword = "RideShare@123";

        // Act
        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        // Assert
        assertThat(firstHash).isNotEqualTo(secondHash);

        // Both hashes should still verify against the same password.
        assertThat(passwordEncoder.matches(rawPassword, firstHash)).isTrue();
        assertThat(passwordEncoder.matches(rawPassword, secondHash)).isTrue();
    }
}