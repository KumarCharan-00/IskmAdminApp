package org.iskm.admin.web.service;

import java.util.Map;

import org.iskm.admin.web.model.entity.Sponsors;
import org.iskm.admin.web.repository.SponsorRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.annotation.PostConstruct;

@Service
public class SponsorService {
	
	@Value("${razorpay.key.id}")
	private String razorpayId;
	
	@Value("${razorpay.key.secret}")
	private String razorpaySecret;
	  
	
	private final SponsorRepository sponsorRepository;

    public SponsorService(SponsorRepository sponsorRepository) {
        this.sponsorRepository = sponsorRepository;
    }
    
    private RazorpayClient razorpayClient;
    
    @PostConstruct
    public void init() throws RazorpayException {
    	this.razorpayClient = new RazorpayClient(razorpayId,razorpaySecret);
    }  
    
    public Sponsors addSponser(Sponsors sponsor) {
    	
    	try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", sponsor.getAmount() * 100);
            orderRequest.put("currency", sponsor.getCurrency());
            orderRequest.put("receipt", "sponsor_" + sponsor.getEmail());

            Order order = razorpayClient.orders.create(orderRequest);

            sponsor.setRazorpayOrderId(order.get("id"));
            sponsor.setRazorpayOrderStatus(order.get("status"));

            return sponsorRepository.save(sponsor);

        } catch (Exception e) {
            throw new RuntimeException("Error while creating Razorpay order: " + e.getMessage(), e);
        }
    }

	public Sponsors updateStatus(Map<String, String> response) {
		Sponsors Sponsors = sponsorRepository.findByRazorpayOrderId(response.get("razorpay_order_id"));
		Sponsors.setIsPaymentDone("Y");
		return sponsorRepository.save(Sponsors);
	}
		
}
