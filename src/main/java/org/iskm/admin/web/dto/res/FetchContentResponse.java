package org.iskm.admin.web.dto.res;

import lombok.Data;

import java.util.List;

@Data
public class FetchContentResponse {
    private int count;
    private List<ContentDTO> content;
}
