package com.protrack.project.service;

/** ISBN-10 / ISBN-13 format + checksum validation (service-layer). */
public final class IsbnValidator {

	private IsbnValidator() {
	}

	/** Strip spaces and hyphens. */
	public static String normalize(String isbn) {
		return isbn.replaceAll("[\\s-]", "");
	}

	public static boolean isValid(String normalized) {
		if (normalized == null) {
			return false;
		}
		return switch (normalized.length()) {
			case 13 -> isValidIsbn13(normalized);
			case 10 -> isValidIsbn10(normalized);
			default -> false;
		};
	}

	private static boolean isValidIsbn13(String s) {
		if (!s.chars().allMatch(Character::isDigit)) {
			return false;
		}
		int sum = 0;
		for (int i = 0; i < 13; i++) {
			int digit = s.charAt(i) - '0';
			sum += (i % 2 == 0) ? digit : digit * 3;
		}
		return sum % 10 == 0;
	}

	private static boolean isValidIsbn10(String s) {
		int sum = 0;
		for (int i = 0; i < 9; i++) {
			char c = s.charAt(i);
			if (!Character.isDigit(c)) {
				return false;
			}
			sum += (c - '0') * (10 - i);
		}
		char last = s.charAt(9);
		int checkValue = (last == 'X' || last == 'x') ? 10 : (Character.isDigit(last) ? last - '0' : -1);
		if (checkValue < 0) {
			return false;
		}
		sum += checkValue;
		return sum % 11 == 0;
	}
}
