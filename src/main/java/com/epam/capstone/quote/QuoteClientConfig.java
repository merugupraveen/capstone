package com.epam.capstone.quote;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quote")
public record QuoteClientConfig(
        String sourceUrl,
        int fetchTimeoutMs
) {
}
