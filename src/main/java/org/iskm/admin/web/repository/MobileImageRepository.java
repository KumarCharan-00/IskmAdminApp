package org.iskm.admin.web.repository;

import java.util.List;

import org.iskm.admin.web.model.entity.MobileImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MobileImageRepository extends JpaRepository<MobileImage, Long>{

	List<MobileImage> findByContentId(String contentId);

}
