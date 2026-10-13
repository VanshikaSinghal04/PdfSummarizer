package com.vanshika.pdf_summarizer.service;

import com.vanshika.pdf_summarizer.model.ApiToken;
import com.vanshika.pdf_summarizer.model.User;
import com.vanshika.pdf_summarizer.repository.ApiTokenRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class ApiTokenService {
    private final ApiTokenRepository repository;
    private final SecureRandom random = new SecureRandom();

    public ApiTokenService(ApiTokenRepository repository) {
        this.repository = repository;
    }

    public String createToken(User user) {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);

        String secretToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        ApiToken token = new ApiToken();
        token.setUser(user);
        token.setTokenHash(hash(secretToken));
        repository.save(token);

        return secretToken;
    }
    public Optional<User> findUserByToken(String secretToken) {
        String tokenHash = hash(secretToken);
        return repository.findByTokenHash(tokenHash)
                .map(ApiToken::getUser);
    }
    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}