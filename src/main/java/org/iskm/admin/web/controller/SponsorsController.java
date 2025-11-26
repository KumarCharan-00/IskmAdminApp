package org.iskm.admin.web.controller;

import java.util.Map;

import org.iskm.admin.web.model.OrderRequestDetails;
import org.iskm.admin.web.model.RPPaymentRequest;
import org.iskm.admin.web.service.SponsorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController("/api")
public class SponsorsController {
	
	private final SponsorService sponsorService;

    public SponsorsController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }
	
	@PostMapping("/generate-pay-req")
    public ResponseEntity<RPPaymentRequest> addSponser(@RequestBody OrderRequestDetails req) {
		log.info("Generating payment request for donor: {}", req);
        return ResponseEntity.ok(sponsorService.generatePaymentRequest(req));
    }
	
	@PostMapping("/payment-callback")
	public String paymentCallback(@RequestParam Map<String,String> response) {
		sponsorService.updateStatus(response);
		return "Success";
		
	}
}
