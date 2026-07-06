package com.protrack.shared.mail;

import com.protrack.shared.properties.ProtrackProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * SMTP {@link MailPort}, active only when {@code protrack.mail.enabled=true} (requires
 * {@code spring.mail.*}). Sends are best-effort: SMTP failures are logged and swallowed so a mail
 * outage never breaks the notification write or the originating request.
 */
@Component
@ConditionalOnProperty(prefix = "protrack.mail", name = "enabled", havingValue = "true")
public class SmtpMailAdapter implements MailPort {

	private static final Logger log = LoggerFactory.getLogger(SmtpMailAdapter.class);

	private final JavaMailSender mailSender;
	private final String from;

	public SmtpMailAdapter(JavaMailSender mailSender, ProtrackProperties properties) {
		this.mailSender = mailSender;
		this.from = properties.mail().from();
	}

	@Override
	public void send(String to, String subject, String body) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (Exception ex) {
			log.warn("[mail:smtp] failed to send to={} subject=\"{}\": {}", to, subject,
					ex.getMessage());
		}
	}
}
