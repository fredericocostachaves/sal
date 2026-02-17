package com.sal.unipile;

import com.sal.unipile.dto.UnipileLinkedInSearchRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/unipile/leads")
@Tag(name = "Unipile Leads", description = "Endpoints para captação de leads via Unipile")
public class UnipileLeadController {

    private final UnipileApiClient unipileApiClient;

    @Value("${unipile.account-id:}")
    private String defaultAccountId;

    @Operation(summary = "Busca leads no LinkedIn", description = "Realiza uma busca de perfis no LinkedIn através da Unipile")
    @PostMapping("/search")
    public ResponseEntity<UnipileLinkedInSearchResponse> searchLeads(
            @Parameter(description = "id da conta linkedln do unipile (conta que irá fazer as requisições)", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileLinkedInSearchRequest request) {

        if (!StringUtils.hasText(accountId)) {
                log.warn("Account ID not provided and no default configured");
                return ResponseEntity.badRequest().build();
        }
        
        request.setAccount_id(accountId.isBlank() ? defaultAccountId : accountId);

        try {
            UnipileLinkedInSearchResponse response = unipileApiClient.searchLinkedIn(request);
            if (response == null) {
                log.warn("Unipile API returned empty response");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} results from Unipile search", response.getItems() != null ? response.getItems().size() : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching leads via Unipile: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
