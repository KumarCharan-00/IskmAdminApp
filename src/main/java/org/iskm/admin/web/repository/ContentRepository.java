package org.iskm.admin.web.repository;

import java.util.List;

import org.iskm.admin.web.model.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContentRepository extends JpaRepository<Content, String>, JpaSpecificationExecutor<Content> {

    List<Content> findByStatus(String status);

}
