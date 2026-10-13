package com.vanshika.pdf_summarizer.repository;

import com.vanshika.pdf_summarizer.model.ApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long> {
    Optional<ApiToken> findByTokenHash(String tokenHash);
}