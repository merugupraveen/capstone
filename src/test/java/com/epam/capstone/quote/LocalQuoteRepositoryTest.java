package com.epam.capstone.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalQuoteRepositoryTest {

    private final LocalQuoteRepository repo = new LocalQuoteRepository(new ObjectMapper());

    @Test
    void shouldAvoidImmediateRepetitionWhenMultipleCandidatesExist() {
        Quote first = repo.randomQuote("Inspirational", null).orElseThrow();
        Quote second = repo.randomQuote("Inspirational", first.key()).orElseThrow();

        // If there are multiple Inspirational quotes, they should differ.
        // If not, the repo may repeat.
        assertThat(second.key()).isNotEqualTo(first.key());
    }

    @Test
    void shouldFilterByCategory() {
        Quote q = repo.randomQuote("Programming", null).orElseThrow();
        assertThat(q.category()).isEqualToIgnoringCase("Programming");
    }
}
