package com.epam.capstone.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class OnlineQuoteClient {

    private final QuoteClientConfig config;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OnlineQuoteClient(QuoteClientConfig config, ObjectMapper objectMapper, RestClient.Builder restClientBuilder) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.restClient = restClientBuilder
                .defaultHeaders(h -> h.setAccept(List.of(MediaType.APPLICATION_JSON)))
                .build();
    }

    public Optional<Quote> tryFetchRandomQuote(String category, String avoidQuoteKey) {
        if (config.sourceUrl() == null || config.sourceUrl().isBlank()) {
            return Optional.empty();
        }

        try {
            String url = config.sourceUrl();
            if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
                // Best-effort. Some APIs may support ?category=...
                url = url + (url.contains("?") ? "&" : "?") + "category=" + encode(category);
            }

            String body = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            if (body == null || body.isBlank()) {
                return Optional.empty();
            }

            Quote mapped = mapToQuote(body);
            if (mapped == null) {
                return Optional.empty();
            }

            if (avoidQuoteKey != null && mapped.key().equals(avoidQuoteKey)) {
                // If the API is returning a single random quote, allow repetition.
                // If it returns a list, mapping already picks random; we could retry but avoid loops.
                return Optional.of(mapped);
            }

            return Optional.of(mapped);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return value.replace(" ", "%20");
    }

    private Quote mapToQuote(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);

            if (root.isArray()) {
                List<Quote> quotes = new ArrayList<>();
                for (JsonNode n : root) {
                    Quote q = mapNode(n);
                    if (q != null) {
                        quotes.add(q);
                    }
                }
                if (quotes.isEmpty()) {
                    return null;
                }
                return quotes.get(ThreadLocalRandom.current().nextInt(quotes.size()));
            }

            if (root.isObject()) {
                return mapNode(root);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Quote mapNode(JsonNode node) {
        String text = firstNonBlank(
                node.path("text").asText(null),
                node.path("quote").asText(null),
                node.path("content").asText(null)
        );

        if (text == null || text.isBlank()) {
            return null;
        }

        String author = firstNonBlank(
                node.path("author").asText(null),
                node.path("by").asText(null)
        );

        String category = firstNonBlank(
                node.path("category").asText(null),
                node.path("tag").asText(null),
                node.path("tags").isArray() && node.path("tags").size() > 0 ? node.path("tags").get(0).asText(null) : null
        );

        String key = node.hasNonNull("id") ? node.get("id").asText() : QuoteKeyUtil.keyOf(text, author);
        return new Quote(key, text, author, category, QuoteSource.ONLINE);
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
