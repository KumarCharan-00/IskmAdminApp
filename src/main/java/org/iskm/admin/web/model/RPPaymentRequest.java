package org.iskm.admin.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RPPaymentRequest {
    private String key;
    private int amount;
    private String currency;
    private String name;
    private String description;
    private String image;
    @JsonProperty("order_id")
    private String orderId;
    // @JsonProperty("callback_url")
    // private String callbackUrl;
    private Prefill prefill;
    private Notes notes;
    private Theme theme;

    @Data
    public static class Prefill {
        private String name;
        private String email;
        private String contact;
    }

    @Data
    public static class Notes {
        private String address;
    }

    @Data
    public static class Theme {
        private String color;
    }
}
