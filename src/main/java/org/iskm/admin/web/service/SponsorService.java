package org.iskm.admin.web.service;

import java.util.Map;
import java.util.Objects;

import org.iskm.admin.web.config.RazorPayConfig;
import org.iskm.admin.web.model.OrderRequestDetails;
import org.iskm.admin.web.model.RPOrderDetails;
import org.iskm.admin.web.model.RPPaymentRequest;
import org.iskm.admin.web.model.entity.Sponsors;
import org.iskm.admin.web.repository.SponsorRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.SneakyThrows;

@Service
public class SponsorService {

    private final ObjectMapper mapper;

    private final RazorpayClient razorpayClient;
	private final SponsorRepository sponsorRepository;
    private RazorPayConfig razorPayConfig;

    // Dependencies are Autowired
    public SponsorService(SponsorRepository sponsorRepository, 
                            RazorpayClient razorpayClient, 
                            RazorPayConfig razorPayConfig,
                            ObjectMapper mapper) {
        this.sponsorRepository = sponsorRepository;
        this.razorpayClient = razorpayClient;
        this.razorPayConfig = razorPayConfig;
        this.mapper = mapper;
    }
    
    @SneakyThrows
    public RPPaymentRequest generatePaymentRequest(OrderRequestDetails req) {
    	try {
            var orderRequest = new JSONObject();
            var notes = new JSONObject();

            orderRequest.put("amount", req.getAmount() * 100);
            orderRequest.put("currency", req.getCurrency());
            orderRequest.put("receipt", "sponsor_" + req.getEmail());
            notes.put("name", req.getName());
            notes.put("email", req.getEmail());
            notes.put("phoneNumber", req.getPhone());
            notes.put("address", req.getFullAddress());
            orderRequest.put("notes", notes);

            var order = razorpayClient.orders.create(orderRequest);

            var orderResponse = mapper.readValue(order.toString(), RPOrderDetails.class);

            if (Objects.nonNull(orderResponse.getId()) && !orderResponse.getId().isBlank()) {
                return createPaymentRequest(orderResponse);
            } else {
                orderResponse.getError();
                return null;
            }

        } catch (RazorpayException rzex) {
            throw new RazorpayException("RazorPay Order Id creation failed: " + rzex.getMessage(), rzex);
        } catch (Exception e) {
            throw new RuntimeException("Error while creating Razorpay order: " + e.getMessage(), e);
        }
    }

    private RPPaymentRequest createPaymentRequest(RPOrderDetails orderResponse) {
        RPPaymentRequest paymentRequest = new RPPaymentRequest();
        
        // Set key from RazorPayConfig
        paymentRequest.setKey(razorPayConfig.razorpayId);
        
        // Map values from order response
        paymentRequest.setAmount(Integer.parseInt(orderResponse.getAmount()));
        paymentRequest.setCurrency(orderResponse.getCurrency());
        paymentRequest.setOrderId(orderResponse.getId());
        
        // Set default values for business
        paymentRequest.setName("ISKM Admin");
        paymentRequest.setDescription("Sponsorship Payment");
        
        // Set prefill information from order notes
        if (orderResponse.getNotes() != null) {
            RPPaymentRequest.Prefill prefill = new RPPaymentRequest.Prefill();
            prefill.setName(orderResponse.getNotes().get("name"));
            prefill.setEmail(orderResponse.getNotes().get("email"));
            prefill.setContact(orderResponse.getNotes().get("phoneNumber"));
            paymentRequest.setPrefill(prefill);

            // Set notes
            RPPaymentRequest.Notes notes = new RPPaymentRequest.Notes();
            notes.setAddress(orderResponse.getNotes().getOrDefault("address", "NOT_SET"));
            paymentRequest.setNotes(notes);
        }
        
        // Set theme
        RPPaymentRequest.Theme theme = new RPPaymentRequest.Theme();
        theme.setColor("#3399cc");
        paymentRequest.setTheme(theme);
        
        return paymentRequest;
    }

	public Sponsors updateStatus(Map<String, String> response) {
		Sponsors Sponsors = sponsorRepository.findByRazorpayOrderId(response.get("razorpay_order_id"));
		Sponsors.setIsPaymentDone("Y");
		return sponsorRepository.save(Sponsors);
	}
		
}
