package org.orbroker.binding.rule;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.orbroker.binding.Column;
import org.orbroker.binding.adapter.BindingAdapters;

public interface BindingRule {
	Object apply(Object target, ResultSet rs, BindingAdapters adapters) throws SQLException;
	void addColumn(Column param);
}

