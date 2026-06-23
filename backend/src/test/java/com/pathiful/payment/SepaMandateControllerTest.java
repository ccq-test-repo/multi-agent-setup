package com.pathiful.payment;

import com.pathiful.auth.TokenService;
import com.pathiful.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SepaMandateController.class)
@AutoConfigureMockMvc(addFilters = false)
class SepaMandateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SepaMandateService sepaMandateService;

    @MockBean
    private TokenService tokenService;

    @BeforeEach
    void setUpSecurityContext() {
        User user = new User("test@example.com", "hash", User.Role.USER);
        user.setId(42L);
        var auth = new UsernamePasswordAuthenticationToken(
                user, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldReturnMandateWhenExists() throws Exception {
        SepaMandateResponse response = new SepaMandateResponse();
        response.setId(1L);
        response.setMandateReference("PATHIFUL-ABCD1234");
        response.setAccountHolderName("Max Mustermann");
        response.setIbanMasked("DE89 **** **** 3000");
        response.setStatus("ACTIVE");

        when(sepaMandateService.getMandate(any())).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/payment/sepa-mandate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mandateReference").value("PATHIFUL-ABCD1234"))
                .andExpect(jsonPath("$.ibanMasked").value("DE89 **** **** 3000"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn404WhenNoMandate() throws Exception {
        when(sepaMandateService.getMandate(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payment/sepa-mandate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateMandate() throws Exception {
        SepaMandateResponse response = new SepaMandateResponse();
        response.setId(2L);
        response.setMandateReference("PATHIFUL-NEWMANDATE");
        response.setAccountHolderName("Erika Musterfrau");
        response.setIbanMasked("DE21 **** **** 1234");
        response.setStatus("ACTIVE");

        when(sepaMandateService.createMandate(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/payment/sepa-mandate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountHolder": "Erika Musterfrau",
                                    "iban": "DE21 3704 0044 0532 0130 00",
                                    "acceptedTerms": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.ibanMasked").value("DE21 **** **** 1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRejectCreateMandateWithoutAccountHolder() throws Exception {
        mockMvc.perform(post("/api/payment/sepa-mandate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "iban": "DE89370400440532013000",
                                    "acceptedTerms": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateMandateWithoutIban() throws Exception {
        mockMvc.perform(post("/api/payment/sepa-mandate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountHolder": "Max Mustermann",
                                    "acceptedTerms": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCreateMandateWithEmptyIban() throws Exception {
        mockMvc.perform(post("/api/payment/sepa-mandate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountHolder": "Max Mustermann",
                                    "iban": "",
                                    "acceptedTerms": true
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRevokeMandate() throws Exception {
        SepaMandateResponse response = new SepaMandateResponse();
        response.setId(1L);
        response.setMandateReference("PATHIFUL-REVOKED");
        response.setIbanMasked("DE89 **** **** 3000");
        response.setStatus("REVOKED");

        when(sepaMandateService.revokeMandate(any())).thenReturn(response);

        mockMvc.perform(put("/api/payment/sepa-mandate/revoke"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
    }
}
