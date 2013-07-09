package org.orbroker;

import static org.orbroker.configuration.BrokerConfigurationFactory.getFactory;

import java.io.File;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.orbroker.binding.adapter.BindingAdapters;
import org.orbroker.configuration.BrokerConfiguration;
import org.orbroker.jdbc.BindingRowMapper;
import org.orbroker.statement.Statement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

//
// if org.springframework.jdbc level is set to DEBUG
// SQL execution will be available in the logs
//
public class Broker{

	private static final Logger logger = Logger.getLogger(Broker.class);

	private BrokerConfiguration config;
	private BindingAdapters adapters;

	public Broker(File configSourcePath, BindingAdapters adapters){
		this(configSourcePath);
		this.adapters = adapters;

		try{
			Velocity.setProperty( Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS,
		      "org.apache.velocity.runtime.log.Log4JLogChute" );

			Velocity.setProperty("runtime.log.logsystem.log4j.logger",
		                    logger.getName());

			Velocity.init();
		} catch (Exception e){
			throw new RuntimeException("Problem initing Velocity", e);
		}
	}

	public Broker(File configSourcePath){
		this(getFactory(configSourcePath).getBrokerConfiguration());
	}

	public Broker(BrokerConfiguration config){
		this.config = config;
	}

	public <T> T fetchOne(String statementIdentifier, DataSource dataSource){
		return this.<T>fetchOne(statementIdentifier, new MapSqlParameterSource(), dataSource);
	}

	public <T> T fetchOne(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		return this.<T>fetchOne(statementIdentifier, params, null, dataSource);
	}

	public <T> T fetchOne(String statementIdentifier, SqlParameterSource sqlParams, Map<String, Object> velocityParams, DataSource dataSource){
		T instance = null;
		Statement stmt = config.getStatement(statementIdentifier);
		String sql =  (velocityParams != null) ? preProcessStatement(stmt, velocityParams) : stmt.getContent();
		NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(dataSource);

		if (stmt.getBindingName() == null){
			SingleColumnRowMapper<T> mapper = new SingleColumnRowMapper<T>();
			instance = tpl.<T>queryForObject(sql, sqlParams, mapper);
		} else {
			BindingRowMapper<T> mapper = new BindingRowMapper<T>(config.getBinding(stmt), this.adapters);
			instance = tpl.<T>queryForObject(sql, sqlParams, mapper);
		}

		return instance;
	}

	public <T> List<T> fetch(String statementIdentifier, DataSource dataSource){
		return this.<T>fetch(statementIdentifier, new MapSqlParameterSource(), null, dataSource);
	}

	public <T> List<T> fetch(String statementIdentifier, SqlParameterSource sqlParams, DataSource dataSource){
		return this.<T>fetch(statementIdentifier, sqlParams, null, dataSource);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetch(String statementIdentifier, SqlParameterSource sqlParams, Map<String, Object> velocityParams, DataSource dataSource){
		Statement stmt = config.getStatement(statementIdentifier);
		String sql =  (velocityParams != null) ? preProcessStatement(stmt, velocityParams) : stmt.getContent();
		NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(dataSource);

		if (stmt.getBindingName() == null){
			return (List<T>) tpl.queryForList(sql, sqlParams);
		} else {
			BindingRowMapper<T> mapper = new BindingRowMapper<T>(config.getBinding(stmt), this.adapters);
			return tpl.query(sql, sqlParams, mapper);
		}
	}

	public <T> T insertOne(String statementIdentifier, SqlParameterSource params, Map<String, Object> velocityParams, DataSource dataSource){
		T key = this.<T>insert(statementIdentifier, params, velocityParams, dataSource);
		if (key == null){
			throw new EmptyResultDataAccessException(1);
		}
		return key;
	}

	public <T> T insertOne(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		return this.<T>insertOne(statementIdentifier, params, null, dataSource);
	}

	public <T> T insert(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		return this.<T>insert(statementIdentifier, params, null, dataSource);
	}

	@SuppressWarnings("unchecked")
	public <T> T insert(String statementIdentifier, SqlParameterSource params, Map<String, Object> velocityParams, DataSource dataSource){
		Statement stmt = config.getStatement(statementIdentifier);
		String sql =  (velocityParams != null) ? preProcessStatement(stmt, velocityParams) : stmt.getContent();
		NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(dataSource);
		KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
		tpl.update(sql, params, generatedKeyHolder);
		return (T) generatedKeyHolder.getKey();
	}

	public long updateOne(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		return updateOne(statementIdentifier, params, null, dataSource);
	}

	public long updateOne(String statementIdentifier, SqlParameterSource params, Map<String, Object> velocityParams, DataSource dataSource){
		long updated = update(statementIdentifier, params, velocityParams, dataSource);
		if (updated == 0){
			throw new EmptyResultDataAccessException(1);
		}
		return updated;
	}

	public long update(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		return update(statementIdentifier, params, null, dataSource);
	}

	public long update(String statementIdentifier, SqlParameterSource params, Map<String, Object> velocityParams, DataSource dataSource){
		Statement stmt = config.getStatement(statementIdentifier);
		String sql =  (velocityParams != null) ? preProcessStatement(stmt, velocityParams) : stmt.getContent();
		NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(dataSource);
		return tpl.update(sql, params);
	}

	public void execute(String statementIdentifier, SqlParameterSource params, DataSource dataSource){
		execute(statementIdentifier, params, null, dataSource);
	}

	public void execute(String statementIdentifier, SqlParameterSource params, Map<String, Object> velocityParams, DataSource dataSource){
		Statement stmt = config.getStatement(statementIdentifier);
		String sql =  (velocityParams != null) ? preProcessStatement(stmt, velocityParams) : stmt.getContent();
		NamedParameterJdbcTemplate tpl = new NamedParameterJdbcTemplate(dataSource);
		tpl.update(sql, params);
	}

	private String preProcessStatement(Statement stmt, Map<String, Object> params){
		StringWriter writer = new StringWriter();
		VelocityContext ctx = new VelocityContext();
		for (Map.Entry<String, Object> entry : params.entrySet()){
			ctx.put(entry.getKey(), entry.getValue());
		}

		try {
			Velocity.evaluate(ctx, writer, stmt.getName(), stmt.getContent());
		} catch (Exception cause) {
			throw new BrokerException("Problem parsing statement " + stmt.getName(), cause);
		}

		return writer.toString();
	}
}
