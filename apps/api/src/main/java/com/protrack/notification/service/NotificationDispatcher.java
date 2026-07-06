package com.protrack.notification.service;

import com.protrack.shared.mail.MailPort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Delivers the email channel off the request/event thread. In-app rows are written synchronously by
 * {@link NotificationService} (they are the durable record); email is a best-effort side-delivery
 * pushed onto {@code notificationExecutor} so SMTP latency never blocks a request or an event
 * listener. Failures are swallowed inside {@link MailPort}.
 */
@Component
public class NotificationDispatcher {

	private final MailPort mailPort;

	public NotificationDispatcher(MailPort mailPort) {
		this.mailPort = mailPort;
	}

	@Async("notificationExecutor")
	public void deliverEmail(String toEmail, String subject, String body) {
		mailPort.send(toEmail, subject, body);
	}
}
