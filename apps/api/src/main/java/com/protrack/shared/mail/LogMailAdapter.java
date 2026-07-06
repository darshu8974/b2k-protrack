package com.protrack.shared.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Default {@link MailPort} for local/dev/demo/test: logs the message instead of sending it. Active
 * unless {@code protrack.mail.enabled=true} selects {@link SmtpMailAdapter}. This keeps the "email
 * channel" fully exercisable (the notification pipeline runs end-to-end) without an SMTP server —
 * satisfying the Phase-1 "email (mock)" definition-of-done.
 */
@Component
@ConditionalOnProperty(prefix = "protrack.mail", name = "enabled", havingValue = "false",
		matchIfMissing = true)
public class LogMailAdapter implements MailPort {

	private static final Logger log = LoggerFactory.getLogger(LogMailAdapter.class);

	@Override
	public void send(String to, String subject, String body) {
		log.info("[mail:log] to={} subject=\"{}\" (SMTP disabled — set protrack.mail.enabled=true "
				+ "to send for real)", to, subject);
	}
}
