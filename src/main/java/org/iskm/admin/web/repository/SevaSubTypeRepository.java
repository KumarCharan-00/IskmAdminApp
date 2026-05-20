package org.iskm.admin.web.repository;

import java.util.List;
import org.iskm.admin.web.model.entity.SevaSubType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SevaSubTypeRepository extends JpaRepository<SevaSubType, String> {
    List<SevaSubType> findBySevaId(String sevaId);
}
