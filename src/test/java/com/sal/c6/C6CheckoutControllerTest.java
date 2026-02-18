package com.sal.c6;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.c6.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(C6CheckoutController.class)
class C6CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private C6CheckoutClient c6CheckoutClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve criar pagamento com sucesso")
    void create_ShouldReturnOk() throws Exception {
        C6CreatePaymentRequest request = C6CreatePaymentRequest.builder()
                .amount(new BigDecimal("100.00"))
                .currency("BRL")
                .description("Teste")
                .paymentMethod("LINK")
                .referenceId("ref-123")
                .build();

        C6CreatePaymentResponse resp = new C6CreatePaymentResponse();
        resp.setId("pay_1");
        resp.setStatus("PENDING");
        resp.setAmount(new BigDecimal("100.00"));
        resp.setCurrency("BRL");
        resp.setCheckoutUrl("https://checkout.example/pay_1");

        when(c6CheckoutClient.createPayment(any())).thenReturn(resp);

        mockMvc.perform(post("/api/c6/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pay_1"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.example/pay_1"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando amount não for informado")
    void create_ShouldReturnBadRequest_WhenNoAmount() throws Exception {
        C6CreatePaymentRequest request = C6CreatePaymentRequest.builder()
                .currency("BRL")
                .build();

        mockMvc.perform(post("/api/c6/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve consultar status com sucesso")
    void status_ShouldReturnOk() throws Exception {
        C6PaymentStatusResponse resp = new C6PaymentStatusResponse();
        resp.setId("pay_1");
        resp.setStatus("PENDING");
        resp.setCurrency("BRL");

        when(c6CheckoutClient.getPaymentStatus(eq("pay_1"))).thenReturn(resp);

        mockMvc.perform(get("/api/c6/checkout/{id}", "pay_1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("pay_1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Deve cancelar pagamento com sucesso")
    void cancel_ShouldReturnNoContent() throws Exception {
        doNothing().when(c6CheckoutClient).cancelPayment(eq("pay_1"));

        mockMvc.perform(post("/api/c6/checkout/{id}/cancel", "pay_1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve estornar pagamento com sucesso")
    void refund_ShouldReturnOk() throws Exception {
        C6RefundRequest request = C6RefundRequest.builder()
                .amount(new BigDecimal("50.00"))
                .reason("Teste")
                .build();

        C6RefundResponse resp = new C6RefundResponse();
        resp.setId("refund_1");
        resp.setPaymentId("pay_1");
        resp.setStatus("APPROVED");
        resp.setRefundedAmount(new BigDecimal("50.00"));

        when(c6CheckoutClient.refundPayment(eq("pay_1"), any())).thenReturn(resp);

        mockMvc.perform(post("/api/c6/checkout/{id}/refund", "pay_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("refund_1"))
                .andExpect(jsonPath("$.paymentId").value("pay_1"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Deve aceitar webhook com sucesso")
    void webhook_ShouldReturnOk() throws Exception {
        Map<String, Object> payload = Map.of("event", "payment.approved", "id", "evt_1");

        mockMvc.perform(post("/api/c6/checkout/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(true));
    }
}
