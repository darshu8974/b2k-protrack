package com.protrack.shared.mail;

/**
 * Outbound email port. The notification module sends through this abstraction so the delivery
 * mechanism (SMTP vs. a dev/test log) is a swappable adapter, never a hard dependency. Sends are
 * best-effort: implementations must not throw into the caller — a failed email must never affect
 * the in-app notification or the originating business operation.
 */
public interface MailPort {

	/**
	 * Deliver a plain-text message. Implementations swallow/log their own failures.
	 *
	 * @param to recipient email address
	 * @param subject message subject
	 * @param body message body (plain text)
	 */
	void send(String to, String subject, String body);
}
