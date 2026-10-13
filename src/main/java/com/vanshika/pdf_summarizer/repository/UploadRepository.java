package com.vanshika.pdf_summarizer.repository;

import com.vanshika.pdf_summarizer.model.Upload;
import com.vanshika.pdf_summarizer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UploadRepository extends JpaRepository<Upload, Long> {
    List<Upload> findByUserOrderByCreatedAtDesc(User user);
    List<Upload> findTop5ByUserOrderByCreatedAtDesc(User user);
    Optional<Upload> findBySummaryKeyAndUser(String summaryKey, User user);
    List<Upload> findByStatus(String status);
}
