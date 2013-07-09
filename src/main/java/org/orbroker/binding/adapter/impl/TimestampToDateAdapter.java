package org.orbroker.binding.adapter.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.orbroker.binding.adapter.BindingAdapter;

public class TimestampToDateAdapter implements BindingAdapter<Timestamp, Date> {
	@Override
	public Class<Timestamp> from() {
		return Timestamp.class;
	}

	@Override
	public Class<Date> to() {
		return Date.class;
	}

	@Override
	public Date valueOf(Object from) {
		return new Date(((Timestamp)from).getTime());
	}
}
