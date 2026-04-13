package com.epam.capstone.quote;

/**
 * Kept for backward compatibility in case older UI/tests rely on it.
 */
@Deprecated
public record QuoteResponse(String text, String author) {
}
