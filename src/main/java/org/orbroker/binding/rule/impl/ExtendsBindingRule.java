package org.orbroker.binding.rule.impl;

import static java.lang.String.format;
import static org.springframework.util.ReflectionUtils.shallowCopyFieldState;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Binding;
import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.binding.rule.DelegateBinding;
import org.orbroker.binding.rule.SingleColumnBindingRule;

public class ExtendsBindingRule extends SingleColumnBindingRule implements DelegateBinding {

	private String condition;
	private Binding delegateBinding;

	public ExtendsBindingRule(Binding binding, String condition) {
		this.delegateBinding = binding;
		if (delegateBinding == null){
			throw new IllegalArgumentException("delegateBinding must be NOT NULL");
		}

		this.condition = condition;
		if (condition == null){
			throw new IllegalStateException("condition must be NOT NULL");
		}
	}

	@Override
	public Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException {
		Object columnValue = rs.getObject(this.column.getName());
		if (columnValue == null || !condition.equals(columnValue.toString())){
			return target;
		}

		Class<?> requiredType = column.hasDestinationType() ?
									column.getDestinationType() : delegateBinding.getRequiredType();

		if (!target.getClass().isAssignableFrom(requiredType)){
			String msg = format("Unable to subclass '%s' with '%s'", target.getClass(), requiredType);
			throw new IllegalStateException(msg);
		}

		Object newTarget = delegateBinding.apply(rs, adapters);
		shallowCopyFieldState(target, newTarget);
		target = null;
		return newTarget;
	}

	@Override
	public Binding getDelegateBinding() {
		return this.delegateBinding;
	}

	public String toString(){
		return format("Column '%s' delegating to %s", column.getName(), delegateBinding.getName());
	}

}
