package com.vanshika.pdf_summarizer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;


@Service
public class S3Service {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.bucket-name}")
    private String bucketName;

    private S3Client s3Client;

    // This runs automatically when Spring Boot starts
    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        System.out.println("✅ S3 connected successfully!");
    }

    // Upload PDF to S3
    public void uploadPdf(String key, MultipartFile file) throws IOException {//this method takes a key (the name of the file in S3) and a MultipartFile (the PDF file uploaded by the user) as parameters. It creates a PutObjectRequest with the bucket name, key, and content type of the file, and then uses the s3Client to upload the file to S3. If the upload is successful, it prints a message to the console.
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        System.out.println("✅ PDF uploaded to S3: " + key);
    }

    // Get summary text from S3
    public String getSummary(String key) throws IOException {//this method takes a key (the name of the file in S3) as a parameter. It creates a GetObjectRequest with the bucket name and key, and then uses the s3Client to download the file from S3. It reads the bytes of the file and converts them to a String using UTF-8 encoding, which is then returned.
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        byte[] bytes = s3Client.getObject(request).readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public boolean summaryExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }
}
