package io.github.jo0yo0n.vitalsjournal.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(TokenProperties.class)
public class JwtConfig {

  @Bean
  public JwtEncoder jwtEncoder(TokenProperties tokenProperties) {

    RSAKey rsaKey =
        new RSAKey.Builder(tokenProperties.publicKey())
            .privateKey(tokenProperties.privateKey())
            .build();
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(TokenProperties tokenProperties) {
    return NimbusJwtDecoder.withPublicKey(tokenProperties.publicKey()).build();
  }
}
