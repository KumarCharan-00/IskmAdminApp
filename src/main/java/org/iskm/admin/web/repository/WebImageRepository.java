package org.iskm.admin.web.repository;

import java.util.List;

import org.iskm.admin.web.model.entity.WebImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebImageRepository extends JpaRepository<WebImage, Long> {

	List<WebImage> findByContentId(String contentId);
}
