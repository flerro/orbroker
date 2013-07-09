package org.orbroker.binding.rule.impl;

import static org.orbroker.support.ReflectionUtils.forName;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Column;
import org.orbroker.binding.adapter.BindingAdapters;

class TypeAndValue {
	Class<?> type = null;
	Object value = null;

	private TypeAndValue(){}

	static TypeAndValue resolveTypeAndValue(ResultSet rs, Column column, BindingAdapters adapters) throws SQLException {

		int columnIndex = rs.findColumn(column.getName());
		Object value = rs.getObject(columnIndex);
		Class<?> type = null;

		if (column.hasDestinationType()){
			type = column.getDestinationType();
			if (value != null && !value.getClass().isAssignableFrom(type)){
				value = adapters.adapterFor(value.getClass(), type).valueOf(value);
			}
		} else {
			type = forName(rs.getMetaData().getColumnClassName(columnIndex));
		}

		TypeAndValue typeAndValue = new TypeAndValue();
		typeAndValue.value = value;
		typeAndValue.type = type;
		return typeAndValue;
	}
}
