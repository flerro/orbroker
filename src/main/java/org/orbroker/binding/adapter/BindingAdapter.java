package org.orbroker.binding.adapter;

public interface BindingAdapter<F, T> {
	public Class<F> from();
	public Class<T> to();
	public T valueOf(Object from);
}
