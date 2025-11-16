package org.iskm.admin.web.model;

import lombok.Data;

@Data
public class OrderRequestDetails {
    private String name;
    private String email;
    private Long phoneNumber;
    private String fullAddress;
    private Long amount;
    private String currency = "INR";
}
