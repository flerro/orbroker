package org.orbroker.statement;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CompoundStatement extends Statement {

	private Map<String, String> conditions = new LinkedHashMap<String, String>();
	private String tail;

	public CompoundStatement() {}

	public CompoundStatement(String name, String content, String bindingName) {
		super(name, content, bindingName);
	}

	public void setTail(String string) {
		this.tail = string;
	}

	public void addCondition(String identifier, String content){
		conditions.put(identifier, purify(content));
	}

	public Set<Statement> statements(){
		Set<Statement> statements = new HashSet<Statement>();
		statements.add(this.copy());

		for (Map.Entry<String, String> condition : conditions.entrySet()){
			String newName = this.getName() + condition.getKey();
			String newContent =  (tail != null) ?
									content + condition.getValue() + tail :
									content + condition.getValue();

			statements.add(new Statement(newName, newContent, bindingName));
		}

		return statements;
	}

	public void importConditionsFrom(CompoundStatement from){
		for (Map.Entry<String, String> entry : from.conditions.entrySet()){
			this.conditions.put(entry.getKey(), entry.getValue());
		}
	}

	public String toString(){
		StringBuilder sb = new StringBuilder("CompoundStatement [ ");
		sb.append(super.toString());
		sb.append(',');
		sb.append(" suffixes=").append(conditions);
		sb.append(" tail=").append(tail);
		sb.append(" ]");
		return sb.toString();
	}

}
