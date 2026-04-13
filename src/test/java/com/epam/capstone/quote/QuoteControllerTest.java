package com.epam.capstone.quote;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "quote.source-url=",
        "quote.fetch-timeout-ms=1"
})
@AutoConfigureMockMvc
class QuoteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void randomShouldReturnQuoteAndUsedFallbackTrueWhenNoOnlineConfigured() throws Exception {
        mockMvc.perform(get("/api/quote/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quote.text").isNotEmpty())
                .andExpect(jsonPath("$.usedFallback").value(true));
    }
}
