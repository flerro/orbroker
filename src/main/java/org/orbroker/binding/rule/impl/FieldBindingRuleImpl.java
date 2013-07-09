package org.orbroker.binding.rule.impl;

import static java.lang.String.format;
import static org.orbroker.binding.rule.impl.TypeAndValue.resolveTypeAndValue;
import static org.orbroker.support.ReflectionUtils.findSetterMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Column;
import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.binding.rule.FieldBindingRule;

public class FieldBindingRuleImpl extends FieldBindingRule {

	public FieldBindingRuleImpl(String propertyName){
		super(propertyName);
	}

	@Override
	public Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException{
		TypeAndValue property = resolveTypeAndValue(rs, column, adapters);
		Method method = findSetterMethod(target.getClass(), propertyName, property.type);
		invokeMethod(method, target, property.value);
		return target;
	}

	@Override
	public void addColumn(Column param) {
		this.column = param;
	}

	@Override
	public String toString() {
		return format("Property '%s' to column '%s'", propertyName, column.getName());
	}

}
