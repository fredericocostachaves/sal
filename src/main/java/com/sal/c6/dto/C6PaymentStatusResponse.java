package com.sal.c6.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class C6PaymentStatusResponse {
    private String id;
    private String status; // PENDING, APPROVED, DECLINED, CANCELLED, REFUNDED, etc.
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // PIX, CARD, LINK
    private BigDecimal paidAmount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private Map<String, Object> additional;
}
