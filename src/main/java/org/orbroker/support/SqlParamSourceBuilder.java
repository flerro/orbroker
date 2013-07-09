package org.orbroker.support;

import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

public class SqlParamSourceBuilder {

	final MapSqlParameterSource params;

	public static SqlParamSourceBuilder sqlParamSource(){
		return new SqlParamSourceBuilder();
	}

	public SqlParamSourceBuilder() {
		params = new MapSqlParameterSource();
	}

	public SqlParamSourceBuilder with(String name, Object obj){
		this.params.addValue(name, obj);
		return this;
	}

	public SqlParameterSource get(){
		return params;
	}

	public SqlParamSourceBuilder withAll(Map<String, ?> map) {
		for (Map.Entry<String, ?> entry : map.entrySet()){
			this.params.addValue(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public SqlParamSourceBuilder withAll(
			Map<String, ?> map, final String fieldPrefix) {
		for (Map.Entry<?, ?> entry : map.entrySet()){
			this.params.addValue(fieldPrefix + entry.getKey(),
					(entry.getValue() == null) ? null : entry.getValue());
		}
		return this;
	}
}
