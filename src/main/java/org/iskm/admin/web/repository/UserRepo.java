package org.iskm.admin.web.repository;

import org.iskm.admin.web.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {

    @Override
    <S extends User> S save(S user);

    <T> T findUserByUserId(String userId, Class<T> classType);
}