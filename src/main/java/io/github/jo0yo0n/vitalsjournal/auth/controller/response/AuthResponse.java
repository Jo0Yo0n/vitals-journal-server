package io.github.jo0yo0n.vitalsjournal.auth.controller.response;

public record AuthResponse(String accessToken, String tokenType) {

  private static final String TOKEN_TYPE = "Bearer";

  public static AuthResponse bearer(String accessToken) {
    return new AuthResponse(accessToken, TOKEN_TYPE);
  }
}
