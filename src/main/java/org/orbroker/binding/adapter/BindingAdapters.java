package org.orbroker.binding.adapter;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class BindingAdapters {

	private Map<String, BindingAdapter> adapters = new HashMap<String, BindingAdapter>();

	public BindingAdapters(List<BindingAdapter> adapters){
		for (BindingAdapter adapter : adapters){
			String key = keyForTypes(adapter.from(), adapter.to());
			this.adapters.put(key, adapter);
		}
	}

	@SuppressWarnings("unchecked")
	public <T,F> BindingAdapter<T,F> adapterFor(Class<F> from, Class<T> to){
		BindingAdapter<T,F> adapter = adapters.get(keyForTypes(from, to));
		if (adapter == null){
			String format = format("No adapter registered for %s -> %s",
										from.getCanonicalName(), to.getCanonicalName());
			throw new IllegalStateException(format);
		}
		return adapter;
	}

	private String keyForTypes(Class<?> from, Class<?> to) {
		return format("From%sTo%s", from.getCanonicalName(), to.getCanonicalName());
	}
}
