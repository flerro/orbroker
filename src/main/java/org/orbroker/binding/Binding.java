package org.orbroker.binding;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.binding.rule.BindingRule;
import org.orbroker.exception.BindingException;
import org.orbroker.exception.BindingRuleException;

public final class Binding {

	private String name = null;
	private Class<?> requiredType = null;
	private List<BindingRule> rules = null;

	public Binding(String name, Class<?> requiredType) {
		this.name = name;
		this.requiredType = requiredType;
	}

	public void setName(String name) {
		this.name = name;
	}
	public void setRequiredType(String requiredTypeName) {
		try {
			this.setRequiredType(Class.forName(requiredTypeName));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public void setRequiredType(Class<?> requiredType) {
		this.requiredType = requiredType;
	}
	public void setRules(List<BindingRule> rules) {
		this.rules = rules;
	}
	public String getName() {
		return name;
	}
	public Class<?> getRequiredType() {
		return requiredType;
	}
	public List<BindingRule> getRules() {
		return rules;
	}
	public void addRule(BindingRule rule){
		if (rules == null){
			rules = new LinkedList<BindingRule>();
		}
		rules.add(rule);
	}
	@Override
	public String toString() {
		return "Binding [" +
				"name=" + name + ", " +
				"requiredType=" + requiredType + ", " +
				"rules=" + rules
				+ "]";
	}

	@SuppressWarnings("unchecked")
	public <T> T apply(ResultSet rs, BindingAdapters adapters) {
		Object target = null;
		try {
			target = getRequiredType().newInstance();
		} catch (Exception e) {
			throw new BindingException(this, e);
		}

		for (BindingRule rule : getRules()){
			try{
				target = rule.apply(target, rs, adapters);
			} catch (BindingRuleException bre) {
				throw bre;
			} catch (Exception e) {
				throw new BindingRuleException(rule, e);
			}
		}

		return (T) target;
	}
}
