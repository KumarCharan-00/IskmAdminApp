package org.iskm.admin.web.util;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.model.entity.User;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CommonUtil {

    public User mapEntityData(AddUserDTO addUserDTO, String creationTime) {
        return new User(addUserDTO.getUserId(),addUserDTO.getUserName(),addUserDTO.getPassword(),creationTime);
    }

}
