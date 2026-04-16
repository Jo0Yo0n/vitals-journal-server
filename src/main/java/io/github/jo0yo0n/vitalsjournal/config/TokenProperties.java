package io.github.jo0yo0n.vitalsjournal.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.token")
public record TokenProperties(
    RSAPublicKey publicKey,
    RSAPrivateKey privateKey,
    Duration accessTokenTtl,
    Duration refreshTokenTtl) {}
