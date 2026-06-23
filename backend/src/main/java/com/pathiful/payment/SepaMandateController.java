package com.pathiful.payment;

import com.pathiful.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für SEPA-Lastschriftmandate.
 *
 * GET  /api/payment/sepa-mandate          – Mandat abrufen (IBAN maskiert)
 * POST /api/payment/sepa-mandate          – Mandat erstellen
 * PUT  /api/payment/sepa-mandate/revoke   – Mandat widerrufen
 */
@RestController
@RequestMapping("/api/payment/sepa-mandate")
public class SepaMandateController {

    private final SepaMandateService sepaMandateService;

    public SepaMandateController(SepaMandateService sepaMandateService) {
        this.sepaMandateService = sepaMandateService;
    }

    @GetMapping
    public ResponseEntity<SepaMandateResponse> getMandate(
            @AuthenticationPrincipal User user) {
        return sepaMandateService.getMandate(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SepaMandateResponse> createMandate(
            @Valid @RequestBody SepaMandateRequest request,
            @AuthenticationPrincipal User user) {
        SepaMandateResponse response = sepaMandateService.createMandate(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/revoke")
    public ResponseEntity<SepaMandateResponse> revokeMandate(
            @AuthenticationPrincipal User user) {
        SepaMandateResponse response = sepaMandateService.revokeMandate(user.getId());
        return ResponseEntity.ok(response);
    }
}
