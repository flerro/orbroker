package org.orbroker.support;

import java.util.LinkedHashMap;
import java.util.Map;

public class MappedParamSourceBuilder {
	final Map<String, Object> params;

	public static MappedParamSourceBuilder mappedParamSource(){
		return new MappedParamSourceBuilder();
	}

	public MappedParamSourceBuilder() {
		params = new LinkedHashMap<String, Object>();
	}

	public MappedParamSourceBuilder with(String name, Object obj){
		this.params.put(name, obj);
		return this;
	}

	public Map<String, Object> get(){
		return params;
	}
}
