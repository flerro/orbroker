package org.orbroker.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Binding;
import org.orbroker.binding.adapter.BindingAdapters;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class BindingRowMapper<T> implements ParameterizedRowMapper<T> {

	private Binding binding = null;
	private BindingAdapters adapters = null;

	public BindingRowMapper(Binding binding, BindingAdapters adapters){
		if (binding == null){
			throw new IllegalArgumentException("Binding MUST NOT be NULL");
		}
		this.binding = binding;
		this.adapters = adapters;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T target = binding.<T>apply(rs, adapters);
		return target;
	}

}
