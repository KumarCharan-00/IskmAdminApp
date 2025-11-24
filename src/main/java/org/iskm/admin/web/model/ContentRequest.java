package org.iskm.admin.web.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContentRequest {

    private String id;
    private String type;
    private String title;
    private String quote;
    private String shortText;
    private String fullText;
    private List<MultipartFile> images;
    private String status;
    private LocalDate showFromDate;
    private LocalDate showToDate;
}
