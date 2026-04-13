package com.epam.capstone.quote;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    private final QuoteService quoteService;
    private final LocalQuoteRepository localQuoteRepository;

    public QuoteController(QuoteService quoteService, LocalQuoteRepository localQuoteRepository) {
        this.quoteService = quoteService;
        this.localQuoteRepository = localQuoteRepository;
    }

    @GetMapping("/random")
    public GetRandomQuoteResponse random(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String avoidQuoteKey
    ) {
        QuoteService.QuoteResult result = quoteService.randomQuote(category, avoidQuoteKey);
        if (result.quote() == null) {
            return new GetRandomQuoteResponse(null, true);
        }
        return new GetRandomQuoteResponse(result.quote(), result.usedFallback());
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return localQuoteRepository.categories();
    }

    public record GetRandomQuoteResponse(Quote quote, boolean usedFallback) {
    }
}
