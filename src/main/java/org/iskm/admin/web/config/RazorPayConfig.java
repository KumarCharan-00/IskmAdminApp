package org.iskm.admin.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Configuration
public class RazorPayConfig {

    @Value("${razorpay.key.id}")
	public String razorpayId;
	
	@Value("${razorpay.key.secret}")
	private String razorpaySecret;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorpayId, razorpaySecret);
    }
    
}
