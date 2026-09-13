package com.bankflow.controller;

import com.bankflow.config.SecurityConfig;
import com.bankflow.dto.CardResponse;
import com.bankflow.dto.IssueCardRequest;
import com.bankflow.entity.Account;
import com.bankflow.entity.Card.CardStatus;
import com.bankflow.entity.Card.CardType;
import com.bankflow.exception.GlobalExceptionHandler;
import com.bankflow.filter.JwtAuthenticationFilter;
import com.bankflow.filter.RateLimitFilter;
import com.bankflow.filter.UserRateLimitFilter;
import com.bankflow.service.CardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CardController.class,
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SecurityConfig.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = RateLimitFilter.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = UserRateLimitFilter.class
                )
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    private CardResponse createCardResponse() {
        return new CardResponse(
                1L,
                "1234567890",
                "**** **** **** 1234",
                CardType.DEBIT,
                CardStatus.ACTIVE,
                Account.AccountStatus.ACTIVE,
                "John Doe",
                LocalDate.of(2030, 12, 31),
                "123",
                new BigDecimal("50000.00")
        );
    }

    @Test
    void issueCard_shouldReturnCreated() throws Exception {
        CardResponse response = createCardResponse();

        when(cardService.issueCard(any(IssueCardRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/cards/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "accountNumber": "1234567890",
                                          "cardType": "DEBIT",
                                          "dailyLimit": 50000.00
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.maskedCardNumber")
                        .value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardType").value("DEBIT"))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$.dailyLimit").value(50000.00));

        verify(cardService).issueCard(any(IssueCardRequest.class));
    }

    @Test
    void getMyCards_shouldReturnCards() throws Exception {
        CardResponse response = createCardResponse();

        when(cardService.getMyCards())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/cards/my-cards")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cardType").value("DEBIT"))
                .andExpect(jsonPath("$[0].cardStatus").value("ACTIVE"));

        verify(cardService).getMyCards();
    }

    @Test
    void toggleCardStatus_shouldReturnUpdatedCard() throws Exception {
        CardResponse response = createCardResponse();

        when(cardService.toggleCardStatus(1L))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/cards/1/toggle-status")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));

        verify(cardService).toggleCardStatus(1L);
    }

    @Test
    void updateDailyLimit_shouldReturnUpdatedCard() throws Exception {
        CardResponse response = createCardResponse();

        BigDecimal newLimit = new BigDecimal("75000.00");

        when(cardService.updateDailyLimit(1L, newLimit))
                .thenReturn(response);

        mockMvc.perform(
                        patch("/api/v1/cards/1/limit")
                                .param("newLimit", "75000.00")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dailyLimit").value(50000.00));

        verify(cardService).updateDailyLimit(1L, newLimit);
    }

    // ==========================================
    // ISSUE CARD VALIDATION TESTS
    // ==========================================

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidIssueCardRequests")
    void issueCard_shouldReturnBadRequest(
            String testCase,
            String requestBody
    ) throws Exception {

        mockMvc.perform(
                        post("/api/v1/cards/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(cardService);
    }

    private static Stream<Arguments> invalidIssueCardRequests() {
        return Stream.of(
                Arguments.of(
                        "Account number missing",
                        """
                        {
                          "cardType": "DEBIT",
                          "dailyLimit": 50000.00
                        }
                        """
                ),
                Arguments.of(
                        "Card type missing",
                        """
                        {
                          "accountNumber": "1234567890",
                          "dailyLimit": 50000.00
                        }
                        """
                ),
                Arguments.of(
                        "Daily limit missing",
                        """
                        {
                          "accountNumber": "1234567890",
                          "cardType": "DEBIT"
                        }
                        """
                ),
                Arguments.of(
                        "Daily limit below minimum",
                        """
                        {
                          "accountNumber": "1234567890",
                          "cardType": "DEBIT",
                          "dailyLimit": 999.99
                        }
                        """
                ),
                Arguments.of(
                        "Account number blank",
                        """
                        {
                          "accountNumber": "",
                          "cardType": "DEBIT",
                          "dailyLimit": 50000.00
                        }
                        """
                )
        );
    }
}