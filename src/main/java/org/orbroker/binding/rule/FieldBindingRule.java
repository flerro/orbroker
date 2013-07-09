package org.orbroker.binding.rule;

import org.orbroker.binding.Column;

public abstract class FieldBindingRule extends SingleColumnBindingRule {

	protected String propertyName;

	public FieldBindingRule(String propertyName){
		super(propertyName);

		if (propertyName == null) {
			throw new IllegalArgumentException("propertyName must be NOT NULL");
		}
		this.propertyName = propertyName;
	}

	public String getPropertyName(){
		return propertyName;
	}
}
