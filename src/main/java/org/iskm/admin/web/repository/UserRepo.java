package org.iskm.admin.web.repository;

import org.iskm.admin.web.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserRepo extends JpaRepository<User, String> {

    @Override
    @NonNull
    <S extends User> S save(@NonNull S user);

    <T> T findUserByUserId(String userId, Class<T> classType);
}
