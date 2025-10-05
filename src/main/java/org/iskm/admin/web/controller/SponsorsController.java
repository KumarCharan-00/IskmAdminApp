package org.iskm.admin.web.controller;

import java.util.Map;

import org.iskm.admin.web.model.entity.Sponsors;
import org.iskm.admin.web.service.SponsorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SponsorsController {
	
	private final SponsorService sponsorService;

    public SponsorsController(SponsorService sponsorService) {
        this.sponsorService = sponsorService;
    }
	
	@PostMapping("/add-sponser")
    public ResponseEntity<Sponsors> addSponser(@RequestBody Sponsors sponsor) {
        return ResponseEntity.ok(sponsorService.addSponser(sponsor));
    }
	
	@PostMapping("/payment-callback")
	public String paymentCallback(@RequestParam Map<String,String> response) {
		sponsorService.updateStatus(response);
		return "Success";
		
	}

}
