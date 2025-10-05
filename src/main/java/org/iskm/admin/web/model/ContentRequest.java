package org.iskm.admin.web.model;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ContentRequest {
	private String id;
    private String pageTitle;
    private String pageContent;
    private List<MultipartFile> webImages;
    private List<MultipartFile> mobileImages;
    private String status;
}
