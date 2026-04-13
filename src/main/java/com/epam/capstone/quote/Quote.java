package com.epam.capstone.quote;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Quote(
        String key,
        String text,
        String author,
        String category,
        QuoteSource source
) {
}
