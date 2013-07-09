package org.orbroker.exception;

import org.orbroker.binding.rule.BindingRule;

@SuppressWarnings("serial")
public class BindingRuleException extends RuntimeException {

	private final BindingRule bindingRule;

	public BindingRuleException(BindingRule rule, Throwable cause) {
		super(cause);
		this.bindingRule = rule;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("Problem binding: ");
		sb.append(bindingRule);
		if (getCause() != null){
			sb.append(" Reason: ").append(getCause().getMessage());
		}
		return sb.toString();
	}

}
