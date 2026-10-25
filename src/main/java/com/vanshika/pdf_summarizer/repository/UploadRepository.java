package com.vanshika.pdf_summarizer.repository;

import com.vanshika.pdf_summarizer.model.Upload;
import com.vanshika.pdf_summarizer.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UploadRepository extends JpaRepository<Upload, Long> {
    List<Upload> findByUserOrderByCreatedAtDesc(User user);
    List<Upload> findTop5ByUserOrderByCreatedAtDesc(User user);
    Optional<Upload> findBySummaryKeyAndUser(String summaryKey, User user);
    List<Upload> findByStatus(String status);
    /**
     * Uses keyset pagination instead of an offset so that adding a new upload
     * does not cause entries to be skipped or duplicated while a client pages.
     */
    @Query("""
        select upload from Upload upload
        where upload.user = :user
          and (:status is null or upload.status = :status)
          and (
              :cursorCreatedAt is null
              or upload.createdAt < :cursorCreatedAt
              or (upload.createdAt = :cursorCreatedAt and upload.id < :cursorId)
          )
        order by upload.createdAt desc, upload.id desc
        """)
    List<Upload> findHistoryPage(
            @Param("user") User user,
            @Param("status") String status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId, Pageable pageable);
}
