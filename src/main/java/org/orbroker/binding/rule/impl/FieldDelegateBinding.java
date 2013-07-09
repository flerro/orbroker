package org.orbroker.binding.rule.impl;

import static java.lang.String.format;
import static org.orbroker.support.ReflectionUtils.findSetterMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Binding;
import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.binding.rule.DelegateBinding;
import org.orbroker.binding.rule.FieldBindingRule;

public class FieldDelegateBinding extends FieldBindingRule implements DelegateBinding {

	private Binding delegateBinding;
	private String condition;

	public FieldDelegateBinding(String propertyName, Binding delegate) {
		super(propertyName);

		this.delegateBinding = delegate;
		if (delegateBinding == null){
			throw new IllegalArgumentException("delegateBinding must be NOT NULL)");
		}
	}

	public void setCondition(String condition){
		this.condition = condition;
	}

	@Override
	public Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException {
		if (this.column.getName() != null && condition != null){
			Object columnValue = rs.getObject(this.column.getName());
			if (columnValue == null || !condition.equals(columnValue.toString())){
				return target;
			}
		}

		Object propertyInstance = delegateBinding.apply(rs, adapters);

		Class<?> requiredType = null;

		if (column.hasDestinationType()){
			requiredType = column.getDestinationType();
			if (!requiredType.isAssignableFrom(delegateBinding.getRequiredType())) {
				String msg = format("Unable to subclass '%s' with '%s'", delegateBinding.getRequiredType(), requiredType);
				throw new IllegalStateException(msg);
			}
		} else {
			requiredType = delegateBinding.getRequiredType();
		}

		Method method = findSetterMethod(target.getClass(), propertyName, requiredType);
		invokeMethod(method, target, propertyInstance);
		return target;
	}

	@Override
	public Binding getDelegateBinding() {
		return delegateBinding;
	}

	public String toString(){
		return format("Property '%s' delegating to %s", propertyName, column.getName(), delegateBinding.getName());
	}
}
