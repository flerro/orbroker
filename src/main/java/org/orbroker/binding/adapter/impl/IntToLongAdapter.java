package org.orbroker.binding.adapter.impl;

import org.orbroker.binding.adapter.BindingAdapter;

public class IntToLongAdapter implements BindingAdapter<Integer, Long> {
	
	@Override
	public Class<Integer> from() {
		return Integer.class;
	}

	@Override
	public Class<Long> to() {
		return Long.class;
	}

	@Override
	public Long valueOf(Object from) {
		return new Long((Integer)from);
	}
}
