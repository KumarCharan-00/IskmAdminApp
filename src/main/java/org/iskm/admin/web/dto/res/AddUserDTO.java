package org.iskm.admin.web.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserDTO {
    private String userId;
    private String userName;
    private String password;
}
