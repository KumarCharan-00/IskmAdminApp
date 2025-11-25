package org.iskm.admin.web.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentUpdateRequest {

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
}
