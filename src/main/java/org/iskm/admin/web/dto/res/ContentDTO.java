package org.iskm.admin.web.dto.res;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDTO extends ErrorDTO {

    private String id;
    private String type;
    private String title;
    private String quote;
    private String previewText;
    private String fullText;
    private String status;
    private String location;
    private Boolean showDonation;
    private LocalDate showFromDate;
    private LocalDate showToDate;
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
