package org.orbroker.configuration;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.orbroker.binding.Binding;
import org.orbroker.statement.CompoundStatement;
import org.orbroker.statement.Statement;

public class BrokerConfiguration {

	private Map<String, Statement> statements = new LinkedHashMap<String, Statement>();
	private Map<String, Binding> bindings = new LinkedHashMap<String, Binding>();

	public BrokerConfiguration(Collection<Statement> statements, Collection<Binding> bindings){
		addAllStatements(statements);
		addAllBindings(bindings);
		
		validate();
	}

	private void validate() {
		for (Statement stmt: this.statements.values()){
			String bindingName = stmt.getBindingName();
			if (bindingName != null && (!this.bindings.containsKey(bindingName))){
				String msg = format("Invalid configuration. Unable to find binding '%s' for statement '%s'", bindingName, stmt.getName());
				throw new IllegalStateException(msg);
			}
		}
	}

	public void addStatement(Statement stmt) {		
		if (stmt instanceof CompoundStatement){
			for (Statement stmt1 : ((CompoundStatement) stmt).statements()){
				this.statements.put(stmt1.getName(), stmt1);
			}
		} else {
			this.statements.put(stmt.getName(), stmt);
		}
	}
	
	public void addAllStatements(Collection<Statement> statements) {
		for (Statement stmt : statements){
			addStatement(stmt);
		}
	}
	
	public void addAllBindings(Collection<Binding> bindings) {
		for (Binding binding : bindings){
			addBinding(binding);
		}
	}

	public void addBinding(Binding binding) {
		this.bindings.put(binding.getName(), binding);
	}
	
	public Statement getStatement(String statementIdentifier) {
		Statement stmt = statements.get(statementIdentifier);
		if (stmt == null){
			throw new IllegalArgumentException(format("Unable to find statement '%s', among %s",
														statementIdentifier, statements.keySet()));
		}
		return stmt;
	}

	public Binding getBinding(Statement stmt) {
		Binding binding = bindings.get(stmt.getBindingName());
		if (binding == null){
			throw new IllegalArgumentException(format("Unable to find binding '%s', among %s",
														stmt.getBindingName(), bindings.keySet()));
		}
		return binding;
	}
	
	public Collection<Binding> getBindings(){
		return Collections.unmodifiableCollection(bindings.values());
	}
	
	public Collection<Statement> getStatements(){
		return Collections.unmodifiableCollection(statements.values());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Broker Configuration: ");
		for (Statement stmt : this.statements.values()){
			sb.append("\t").append(stmt);
		}

		for (Binding binding : this.bindings.values()){
			sb.append("\t").append(binding);
		}		
		return sb.toString();
	}
}