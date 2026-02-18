package com.sal.c6;

import com.sal.c6.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/c6/checkout")
@Tag(name = "C6 Checkout", description = "Integração com C6 Bank Checkout (sandbox)")
public class C6CheckoutController {

    private final C6CheckoutClient c6CheckoutClient;

    @Value("${c6.checkout.default-currency:BRL}")
    private String defaultCurrency;

    @Operation(summary = "Cria um pagamento/ordem de checkout")
    @PostMapping
    public ResponseEntity<C6CreatePaymentResponse> create(@RequestBody C6CreatePaymentRequest request) {
        if (request.getAmount() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (!StringUtils.hasText(request.getCurrency())) {
            request.setCurrency(defaultCurrency);
        }
        C6CreatePaymentResponse response = c6CheckoutClient.createPayment(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Consulta status do pagamento")
    @GetMapping("/{paymentId}")
    public ResponseEntity<C6PaymentStatusResponse> status(@PathVariable String paymentId) {
        C6PaymentStatusResponse response = c6CheckoutClient.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancela um pagamento")
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String paymentId) {
        c6CheckoutClient.cancelPayment(paymentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Estorna um pagamento")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<C6RefundResponse> refund(@PathVariable String paymentId,
                                                   @RequestBody(required = false) C6RefundRequest request) {
        if (request == null) request = new C6RefundRequest();
        C6RefundResponse response = c6CheckoutClient.refundPayment(paymentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Webhook de notificações do C6")
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(@RequestHeader Map<String, String> headers,
                                                       @RequestBody Map<String, Object> payload) {
        // TODO: Validar assinatura (quando o segredo estiver configurado)
        log.info("[C6][WEBHOOK] headers={}, payload={}", headers, payload);
        return ResponseEntity.ok(Map.of("received", true));
    }
}
