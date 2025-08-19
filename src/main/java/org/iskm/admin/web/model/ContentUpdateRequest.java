package org.iskm.admin.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentUpdateRequest {
    private String pageTitle;
    private String pageContent;
    private String status;
}
