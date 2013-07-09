package org.orbroker.binding.adapter.impl;

import org.orbroker.binding.adapter.BindingAdapter;

public class LongToIntAdapter implements BindingAdapter<Long, Integer> {

	@Override
	public Class<Long> from() {
		return Long.class;
	}

	@Override
	public Class<Integer> to() {
		return Integer.class;
	}

	@Override
	public Integer valueOf(Object from) {
		int i = (int)((Long)from / 1);
		return new Integer(i);
	}
}
