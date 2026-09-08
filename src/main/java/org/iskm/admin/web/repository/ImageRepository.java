package org.iskm.admin.web.repository;

import java.util.List;

import org.iskm.admin.web.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByContentId(String contentId);
}
