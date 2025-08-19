package org.iskm.admin.web.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentRequest {
    private String pageTitle;
    private String pageContent;
    private MultipartFile image;
    private String status;
}
