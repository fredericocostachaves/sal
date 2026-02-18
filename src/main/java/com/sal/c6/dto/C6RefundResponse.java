package com.sal.c6.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class C6RefundResponse {
    private String id;
    private String paymentId;
    private String status; // REQUESTED, APPROVED, DECLINED, etc.
    private BigDecimal refundedAmount;
    private OffsetDateTime createdAt;
}
