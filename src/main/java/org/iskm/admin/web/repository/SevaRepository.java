package org.iskm.admin.web.repository;

import org.iskm.admin.web.model.entity.Seva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SevaRepository extends JpaRepository<Seva, String> {
}
