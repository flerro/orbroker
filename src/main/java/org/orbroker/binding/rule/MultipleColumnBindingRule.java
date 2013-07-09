package org.orbroker.binding.rule;


import java.util.LinkedList;
import java.util.List;

import org.orbroker.binding.Column;


public abstract class MultipleColumnBindingRule implements BindingRule {

	protected List<Column> columns = new LinkedList<Column>();

	@Override
	public void addColumn(Column param) {
		if (columns == null){
			columns = new LinkedList<Column>();
		}
		columns.add(param);
	}
}
