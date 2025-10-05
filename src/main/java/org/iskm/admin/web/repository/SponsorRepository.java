package org.iskm.admin.web.repository;

import org.iskm.admin.web.model.entity.Sponsors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SponsorRepository extends JpaRepository<Sponsors, Long> {

	Sponsors findByRazorpayOrderId(String razorpayId);
	
}

