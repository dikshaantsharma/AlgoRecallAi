package com.algorecall.api.controller;

import com.algorecall.api.dto.UploadDocumentRequest;
import com.algorecall.api.dto.QueryDocumentRequest;
import com.algorecall.api.dto.QueryDocumentResponse;
import com.algorecall.api.model.User;
import com.algorecall.api.service.DocumentRagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentRagController {

    private final DocumentRagService documentRagService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UploadDocumentRequest request
    ) {
        documentRagService.uploadDocument(user.getId(), request);
        return ResponseEntity.ok("Document uploaded and indexed successfully.");
    }

    @PostMapping(value = "/upload/pdf", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(
            @AuthenticationPrincipal User user,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file
    ) {
        documentRagService.uploadPdf(user.getId(), file);
        return ResponseEntity.ok("PDF document parsed and indexed successfully.");
    }

    @PostMapping("/query")
    public ResponseEntity<QueryDocumentResponse> queryDocument(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody QueryDocumentRequest request
    ) {
        String answer = documentRagService.queryDocuments(user.getId(), request.getQuestion());
        return ResponseEntity.ok(new QueryDocumentResponse(answer));
    }
}
