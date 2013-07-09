package org.orbroker.binding.rule.impl;

import static java.lang.String.format;
import static org.orbroker.binding.rule.impl.TypeAndValue.resolveTypeAndValue;
import static org.orbroker.support.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Column;
import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.binding.rule.MultipleColumnBindingRule;

public class MethodBindingRule extends MultipleColumnBindingRule {

	protected String methodName = null;

	public MethodBindingRule(String methodName) {
		this.methodName = methodName;
	}

	public Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException {
		int columnsCount = columns.size();
		Object[] values = new Object[columnsCount];
		Class<?>[] argumentTypes = new Class<?>[columnsCount];

		for (int current = 0; current < columnsCount; current++) {
			Column column = columns.get(current);
			TypeAndValue param = resolveTypeAndValue(rs, column, adapters);
			values[current] = param.value;
			argumentTypes[current] = param.type;
		}

		Method method = findMethod(target.getClass(), methodName, argumentTypes);
		invokeMethod(method, target, values);
		return target;
	}

	@Override
	public String toString() {
		return format("Method '%s' with columns: %s", methodName, columns);
	}

}
