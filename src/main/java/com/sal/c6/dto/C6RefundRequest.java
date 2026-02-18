package com.sal.c6.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class C6RefundRequest {
    private BigDecimal amount; // valor a estornar (opcional: total se nulo)
    private String reason;     // motivo do estorno (opcional)
}
