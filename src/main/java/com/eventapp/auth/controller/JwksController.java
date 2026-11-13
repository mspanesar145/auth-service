package com.eventapp.auth.controller;

import com.eventapp.auth.service.TokenService;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
public class JwksController {
    private final TokenService tokens;

    public JwksController(TokenService t) {
        tokens = t;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey key = new RSAKey.Builder((RSAPublicKey) tokens.getPublicKey()).keyID("auth-key-1").algorithm(JWSAlgorithm.RS256).keyUse(KeyUse.SIGNATURE).build();
        return new JWKSet(key).toJSONObject();
    }
}
