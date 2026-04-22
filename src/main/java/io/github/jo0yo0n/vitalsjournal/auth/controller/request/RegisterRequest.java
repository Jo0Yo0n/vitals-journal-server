package io.github.jo0yo0n.vitalsjournal.auth.controller.request;

import io.github.jo0yo0n.vitalsjournal.common.validation.Utf8ByteLength;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email @Size(max = 320) String email,
    @NotBlank @Size(min = 8) @Utf8ByteLength(max = 72) String password,
    @NotBlank @Size(max = 50) String nickname) {}
