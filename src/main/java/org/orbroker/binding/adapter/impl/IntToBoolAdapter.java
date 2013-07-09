package org.orbroker.binding.adapter.impl;

import org.orbroker.binding.adapter.BindingAdapter;

public class IntToBoolAdapter implements BindingAdapter<Integer, Boolean> {
	
	@Override
	public Class<Integer> from() {
		return Integer.class;
	}

	@Override
	public Class<Boolean> to() {
		return Boolean.class;
	}

	@Override
	public Boolean valueOf(Object from) {		
		return (((Integer)from) == 1);
	}
}
