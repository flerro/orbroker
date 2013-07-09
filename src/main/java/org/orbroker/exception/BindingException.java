package org.orbroker.exception;

import org.orbroker.binding.Binding;
import static java.lang.String.format;

@SuppressWarnings("serial")
public class BindingException extends RuntimeException {

	private final Binding binding;

	public BindingException(Binding binding, Throwable cause) {
		super(cause);
		this.binding = binding;
	}

	@Override
	public String getMessage() {
		String reason = (getCause() != null) ? getCause().getMessage() : "unknown";
		return format("Problem with binding: %s. Reason: %s", binding.getName(), reason);
	}
}
