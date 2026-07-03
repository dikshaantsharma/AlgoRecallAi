package com.algorecall.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequest {
    @NotBlank(message = "Document title is required")
    private String title;

    @NotBlank(message = "Document content is required")
    private String content;
}
