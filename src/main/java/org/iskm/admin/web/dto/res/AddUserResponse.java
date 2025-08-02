package org.iskm.admin.web.dto.res;

import lombok.Getter;

@Getter
public class AddUserResponse extends SuccessResponse {

    public AddUserResponse() {
        this(false);
    }

    public AddUserResponse(boolean success) {
        super(success);
    }
}
