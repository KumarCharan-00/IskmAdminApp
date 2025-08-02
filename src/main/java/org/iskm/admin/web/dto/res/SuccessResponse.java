package org.iskm.admin.web.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SuccessResponse implements Response {
    private boolean success;
}
