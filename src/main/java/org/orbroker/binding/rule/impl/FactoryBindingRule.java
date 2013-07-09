package org.orbroker.binding.rule.impl;

import static java.lang.String.format;
import static org.orbroker.binding.rule.impl.TypeAndValue.resolveTypeAndValue;
import static org.orbroker.support.ReflectionUtils.findMethod;
import static org.orbroker.support.ReflectionUtils.findSetterMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.adapter.BindingAdapters;

public class FactoryBindingRule extends FieldBindingRuleImpl {

	private Class<?> factoryClass;
	private String methodName;

	public FactoryBindingRule(String propertyName, Class<?> factoryClass, String methodName) {
		super(propertyName);
		this.factoryClass = factoryClass;
		if (factoryClass == null) {
			throw new IllegalArgumentException("factoryClass must be NOT NULL");
		}

		this.methodName = methodName;
		if (methodName == null) {
			throw new IllegalArgumentException("methodName must be NOT NULL");
		}
	}

	@Override
	public Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException {
		TypeAndValue factoryParam = resolveTypeAndValue(rs, column, adapters);

		Method method = findMethod(factoryClass, methodName, factoryParam.type);
		Object factoryResult = invokeMethod(method, factoryClass, factoryParam.value);

		if (factoryResult == null){
			String format = format("Factory %s.%s returned a null value", factoryClass.getCanonicalName(), methodName);
			throw new IllegalStateException(format);
		}

		Method setter = findSetterMethod(target.getClass(), propertyName, factoryResult.getClass());
		invokeMethod(setter, target, factoryResult);
		return target;
	}

	@Override
	public String toString() {
		return format("Property '%s' to column '%s', using factory %s.%s", propertyName, column.getName(), methodName, factoryClass);
	}
}
