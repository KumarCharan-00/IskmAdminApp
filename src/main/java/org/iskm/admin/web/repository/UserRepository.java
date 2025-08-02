package org.iskm.admin.web.repository;

import java.util.Optional;

import org.iskm.admin.web.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String userName);
}