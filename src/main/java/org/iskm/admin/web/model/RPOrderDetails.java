package org.iskm.admin.web.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class RPOrderDetails {
    private String id;
    private String entity;
    private String amount;
    @JsonAlias("amount_paid")
    private int amountPaid;
    @JsonAlias("amount_due")
    private int amountDue;
    private String currency;
    private String receipt;
    @JsonAlias("offer_id")
    private String offerId;
    private String status;
    private int attempts;
    private Map<String, String> notes;
    @JsonAlias("created_at")
    private String createdAt;
    private Error error;

    @Data
    public static class Error {
        private String code;
        private String description;
        private String source;
        private String step;
        private String reason;
        private String field;
    }
}
