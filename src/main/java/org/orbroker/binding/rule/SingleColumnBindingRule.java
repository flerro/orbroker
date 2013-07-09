package org.orbroker.binding.rule;

import org.orbroker.binding.Column;

public abstract class SingleColumnBindingRule implements BindingRule{
	
	protected Column column;
	
	public SingleColumnBindingRule(){ 
		this("unknown");
	}
	
	public SingleColumnBindingRule(String columnName){
		if (columnName == null) {
			throw new IllegalArgumentException("columnName must be NOT NULL");
		}
		this.column = new Column(columnName);
	}
	
	@Override
	public void addColumn(Column param) {
		this.column = param;
	}
	public Column getColumn() {
		return column;
	}

}
