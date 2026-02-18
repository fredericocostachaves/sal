package com.sal.c6.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class C6CreatePaymentResponse {
    private String id;
    private String status; // e.g., CREATED, PENDING, APPROVED, DECLINED
    private BigDecimal amount;
    private String currency;
    // Campos úteis para diferentes meios de pagamento
    private String checkoutUrl;   // Link para o Checkout (LINK/CARD)
    private String qrCode;        // Imagem base64 ou payload do QR
    private String pixCopyPaste;  // Chave copia e cola PIX

    private Map<String, Object> additional; // resposta completa/extra se necessário
}
