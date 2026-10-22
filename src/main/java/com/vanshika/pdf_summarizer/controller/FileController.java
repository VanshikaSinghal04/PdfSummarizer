package com.vanshika.pdf_summarizer.controller;
import com.vanshika.pdf_summarizer.model.Upload;
import com.vanshika.pdf_summarizer.model.User;
import com.vanshika.pdf_summarizer.repository.UploadRepository;
import com.vanshika.pdf_summarizer.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.annotation.Autowired;
import com.vanshika.pdf_summarizer.service.S3Service;
import com.vanshika.pdf_summarizer.service.WebhookService;
import com.vanshika.pdf_summarizer.service.ApiTokenService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

@Controller//this tell the spring that this class will listen to incoming http request and respond to them.
@CrossOrigin(origins = "*")//this allow frontend and backend to communicate with each other even if they are on different ports.
public class FileController {

    @Autowired //this tells Spring to automatically create an instance of S3Service and inject it into this controller, so we can use it to interact with AWS S3.
    private S3Service s3Service;

    @Autowired
    private WebhookService webhookService;
    @Autowired
    private UserService userService;

    @Autowired
    private UploadRepository uploadRepository;
    @Autowired
    private ApiTokenService apiTokenService;

    
    @GetMapping("/")//this tells spring that when a user visits the root URL of the website, it should call this method to handle the request.
    public ModelAndView home() {//this method returns a ModelAndView object that tells Spring to redirect the user to the index.html page when they visit the root URL.
        return new org.springframework.web.servlet.ModelAndView(
            "redirect:/index.html");
    }

    // GET /upload-page - shows upload page after login
    @GetMapping("/upload-page")
    public ModelAndView uploadPage() {//this method returns a ModelAndView object that tells Spring to redirect the user to the upload-page.html page when they visit the /upload-page URL.
        return new org.springframework.web.servlet.ModelAndView(
            "redirect:/upload-page.html");
    }

    // POST /upload - receives PDF from browser
    
    @ResponseBody
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(//this method handles the file upload request from the frontend. It takes in a MultipartFile object representing the uploaded PDF file and an OAuth2User object representing the authenticated user.
            @RequestParam("pdf") MultipartFile file,
            @AuthenticationPrincipal OAuth2User principal) {

        Map<String, String> response = new HashMap<>();

        try {
            // Check if file is PDF
            if (!file.getContentType().equals("application/pdf")) {//this checks if the uploaded file is a PDF by checking its content type. If it's not a PDF, it returns a bad request response with an error message.
                response.put("error", "Only PDF files allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // Get logged in user
            String googleId = principal.getAttribute("sub");//this retrieves the Google ID of the authenticated user from the OAuth2User object. The Google ID is used to identify the user in the database.
            User user = userService.findByGoogleId(googleId);//this calls the findByGoogleId method of the UserService to check if the user already exists in the database. If the user doesn't exist, it creates a new user record in the database using the saveOrUpdateUser method of the UserService.
            if (user == null) {
                user = userService.saveOrUpdateUser(principal);
            }

            // Create unique file name
            long timestamp = System.currentTimeMillis();
            String safeName = file.getOriginalFilename().replace(" ", "_");
            String pdfKey = "pdfs/" + timestamp + "_" + safeName;
            String summaryKey = "summaries/" + timestamp + "_" + safeName + ".txt";

            // Upload to S3
            s3Service.uploadPdf(pdfKey, file);

            // Save to database
            Upload upload = new Upload();
            upload.setUser(user);
            upload.setOriginalFilename(file.getOriginalFilename());
            upload.setPdfKey(pdfKey);
            upload.setSummaryKey(summaryKey);
            upload.setStatus("PROCESSING");
            uploadRepository.save(upload);

            // Call n8n webhook
            webhookService.triggerN8n(pdfKey, summaryKey);

            // Return jobId to frontend
            response.put("message", "PDF uploaded! Summarizing...");
            response.put("jobId", summaryKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    @ResponseBody
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
        @AuthenticationPrincipal OAuth2User principal)
    {
        try {
        // Get logged in user
        String googleId = principal.getAttribute("sub");
        User user = userService.findByGoogleId(googleId);

        // Get all uploads for this user
        List<Upload> uploads = uploadRepository
            .findTop5ByUserOrderByCreatedAtDesc(user);

        // Convert to simple map list
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Upload upload : uploads) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", upload.getId());
            item.put("filename", upload.getOriginalFilename());
            item.put("summaryKey", upload.getSummaryKey());
            item.put("status", upload.getStatus());
            item.put("createdAt", upload.getCreatedAt().toString());
            result.add(item);
        }

        return ResponseEntity.ok(result);

    } catch (Exception e) {
        return ResponseEntity.internalServerError().build();
    }
}
    @PostMapping("/api-tokens")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createApiToken(
            @AuthenticationPrincipal OAuth2User principal) {

        User user = userService.saveOrUpdateUser(principal);
        String token = apiTokenService.createToken(user);

        return ResponseEntity.ok(Map.of("token", token));
    }


    @ResponseBody
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam("jobId") String jobId,
            @AuthenticationPrincipal Jwt jwt) {

        Map<String, Object> response = new HashMap<>();

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Long userId = Long.valueOf(jwt.getSubject());
        User user = userService.findById(userId);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Upload upload = uploadRepository
                .findBySummaryKeyAndUser(jobId, user)
                .orElse(null);

        if (upload == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            if (!s3Service.summaryExists(upload.getSummaryKey())) {
                response.put("ready", false);
                return ResponseEntity.ok(response);
            }

            String summary = s3Service.getSummary(upload.getSummaryKey());
            response.put("ready", true);
            response.put("summary", summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
