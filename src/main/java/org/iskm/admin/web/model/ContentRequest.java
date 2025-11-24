package org.iskm.admin.web.model;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentRequest {

    private String id;
    private String pageTitle;
    private String pageContent;
    private List<MultipartFile> images;
    private String status;
}
