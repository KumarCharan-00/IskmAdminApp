package org.iskm.admin.web.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDTO {
    private String id;
    private String pageTitle;
    private String pageContent;
    private String status;
    private LocalDateTime createdAt;
    private List<ImageDTO> images;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageDTO {
        private Long id;
        private byte[] imageData;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
    }
}
