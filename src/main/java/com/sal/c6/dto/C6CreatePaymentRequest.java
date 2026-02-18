package com.sal.c6.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class C6CreatePaymentRequest {
    private BigDecimal amount; // Ex.: 100.50
    private String currency;   // Ex.: BRL
    private String description;
    private String referenceId; // Referência interna do pedido
    private String paymentMethod; // Ex.: PIX, CARD, LINK
    private String returnUrl; // URL de retorno após pagamento
    private String cancelUrl; // URL de cancelamento
    private Customer customer; // Dados do cliente (opcional)

    // Campo flexível para extensões específicas da API do C6
    private Map<String, Object> additional;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Customer {
        private String name;
        private String email;
        private String document; // CPF/CNPJ
        private String phone;
    }
}
