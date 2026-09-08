package org.iskm.admin.web.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "sponsors")
public class Sponsors {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sponserId;
	
	@Column(nullable = false)
	private String sponserName;
	
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(nullable = true, unique = true)
	private Long phoneNumber;
	
	private String fullAddress;
	
	@Column(nullable = false)
	private Double amount;
	
	private String razorpayOrderStatus;
		
	@Column(name = "razorpay_order_id")
	private String razorpayOrderId;
	
	private String currency = "INR";
	
	private String isPaymentDone;

}
