package com.epam.capstone.quote;

import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private final OnlineQuoteClient onlineQuoteClient;
    private final LocalQuoteRepository localQuoteRepository;

    public QuoteService(OnlineQuoteClient onlineQuoteClient, LocalQuoteRepository localQuoteRepository) {
        this.onlineQuoteClient = onlineQuoteClient;
        this.localQuoteRepository = localQuoteRepository;
    }

    public QuoteResult randomQuote(String category, String avoidQuoteKey) {
        return onlineQuoteClient.tryFetchRandomQuote(category, avoidQuoteKey)
                .map(q -> new QuoteResult(q, false))
                .orElseGet(() -> localQuoteRepository.randomQuote(category, avoidQuoteKey)
                        .map(q -> new QuoteResult(q, true))
                        .orElseGet(() -> new QuoteResult(null, true)));
    }

    public record QuoteResult(Quote quote, boolean usedFallback) {
    }
}
