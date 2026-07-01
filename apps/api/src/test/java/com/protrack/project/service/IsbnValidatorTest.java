package com.protrack.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link IsbnValidator} (no Spring context, no Docker). */
class IsbnValidatorTest {

	@Test
	void acceptsValidIsbn13() {
		assertThat(IsbnValidator.isValid("9780306406157")).isTrue();
	}

	@Test
	void acceptsValidIsbn10() {
		assertThat(IsbnValidator.isValid("0306406152")).isTrue();
	}

	@Test
	void rejectsBadChecksum() {
		// The prototype's sample ISBN — fails the ISBN-13 checksum.
		assertThat(IsbnValidator.isValid("9781316519027")).isFalse();
	}

	@Test
	void rejectsBadFormat() {
		assertThat(IsbnValidator.isValid("123")).isFalse();
		assertThat(IsbnValidator.isValid("notanisbn12")).isFalse();
	}

	@Test
	void normalizeStripsHyphensAndSpaces() {
		assertThat(IsbnValidator.normalize("978-0-306-40615-7")).isEqualTo("9780306406157");
	}
}
