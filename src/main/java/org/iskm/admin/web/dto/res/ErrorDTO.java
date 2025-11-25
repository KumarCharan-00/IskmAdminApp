package org.iskm.admin.web.dto.res;

import lombok.Data;

@Data
public class ErrorDTO implements Response {

    private String failureMsg;
}
