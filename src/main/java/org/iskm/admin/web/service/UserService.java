package org.iskm.admin.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.iskm.admin.web.dto.res.AddUserDTO;
import org.iskm.admin.web.dto.res.AddUserResponse;
import org.iskm.admin.web.dto.res.Response;
import org.iskm.admin.web.repository.UserRepo;
import org.iskm.admin.web.util.CommonUtil;
import org.iskm.admin.web.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {
	
	private final UserRepo repo;
	 private final CommonUtil commonUtil;
	
	@Autowired
    public UserService(UserRepo repo,CommonUtil commonUtil) {
        this.repo = repo;
        this.commonUtil = commonUtil;
    }
	
    public Response saveUser(AddUserDTO addUserDTO) {
		AddUserResponse res;
        try {
            var creationTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
            addUserDTO.setPassword(PasswordUtil.encodePassword(addUserDTO.getPassword()));
            var userEntity = commonUtil.mapEntityData(addUserDTO, creationTime);
            repo.save(userEntity);
            res = new AddUserResponse(true);
        } catch (Exception ex) {
            log.error("Adding user failed with error :: ", ex);
            res = new AddUserResponse(false);
        }
        return res;
    }
	


}
