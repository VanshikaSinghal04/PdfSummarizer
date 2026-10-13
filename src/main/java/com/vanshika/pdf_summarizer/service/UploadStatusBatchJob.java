package com.vanshika.pdf_summarizer.service;

import com.vanshika.pdf_summarizer.model.Upload;
import com.vanshika.pdf_summarizer.repository.UploadRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UploadStatusBatchJob {
    private final UploadRepository uploadRepository;
    private final S3Service s3Service;

    public UploadStatusBatchJob(
            UploadRepository uploadRepository,
            S3Service s3Service) {
        this.uploadRepository = uploadRepository;
        this.s3Service = s3Service;
    }

    @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Kolkata")
    public void markCompletedUploads() {
        List<Upload> uploads = uploadRepository.findByStatus("PROCESSING");

        for (Upload upload : uploads) {
            if (s3Service.summaryExists(upload.getSummaryKey())) {
                upload.setStatus("COMPLETED");
                uploadRepository.save(upload);
            }
        }
    }
}