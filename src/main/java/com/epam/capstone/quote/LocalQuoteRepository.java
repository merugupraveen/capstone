package com.epam.capstone.quote;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class LocalQuoteRepository {

    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    private volatile List<LocalQuote> cached;

    public LocalQuoteRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<Quote> randomQuote(String category, String avoidQuoteKey) {
        List<LocalQuote> all = load();

        List<LocalQuote> candidates = all.stream()
                .filter(q -> category == null || category.isBlank() || "all".equalsIgnoreCase(category) || matchesCategory(q.category(), category))
                .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        if (avoidQuoteKey != null && candidates.size() > 1) {
            List<LocalQuote> filtered = candidates.stream()
                    .filter(q -> !QuoteKeyUtil.keyOf(q.text(), q.author()).equals(avoidQuoteKey))
                    .toList();
            if (!filtered.isEmpty()) {
                candidates = filtered;
            }
        }

        LocalQuote picked = candidates.get(random.nextInt(candidates.size()));
        String key = QuoteKeyUtil.keyOf(picked.text(), picked.author());
        return Optional.of(new Quote(key, picked.text(), picked.author(), picked.category(), QuoteSource.LOCAL));
    }

    public List<String> categories() {
        return load().stream()
                .map(LocalQuote::category)
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    private boolean matchesCategory(String quoteCategory, String selectedCategory) {
        if (quoteCategory == null || quoteCategory.isBlank()) {
            return false;
        }
        return quoteCategory.trim().toLowerCase(Locale.ROOT)
                .equals(selectedCategory.trim().toLowerCase(Locale.ROOT));
    }

    private List<LocalQuote> load() {
        List<LocalQuote> local = cached;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cached != null) {
                return cached;
            }
            try {
                ClassPathResource res = new ClassPathResource("quotes-fallback.json");
                cached = objectMapper.readValue(res.getInputStream(), new TypeReference<>() {
                });
                return cached;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load fallback quotes", e);
            }
        }
    }

    private record LocalQuote(String text, String author, String category) {
    }
}
