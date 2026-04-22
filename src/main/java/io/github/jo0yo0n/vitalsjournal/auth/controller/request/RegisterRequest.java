package io.github.jo0yo0n.vitalsjournal.auth.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank
        @Size(min = 8, max = 72)
        @Pattern(regexp = "^[A-Za-z0-9!@#$%&*._?-]+$", message = "invalid")
        String password,
    @NotBlank @Size(max = 50) String nickname) {}
