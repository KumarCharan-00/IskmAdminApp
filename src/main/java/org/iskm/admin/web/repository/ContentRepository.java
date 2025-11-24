package org.iskm.admin.web.repository;

import java.util.List;

import org.iskm.admin.web.model.entity.Content;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, String> {

    List<Content> findByStatus(String status);

    List<Content> findAll(Specification<Content> spec);

}
