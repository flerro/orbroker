package org.orbroker;

import static java.lang.String.format;
import static org.orbroker.support.ReflectionUtils.forName;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.orbroker.binding.Binding;
import org.orbroker.binding.Column;
import org.orbroker.binding.rule.BindingRule;
import org.orbroker.binding.rule.impl.ExtendsBindingRule;
import org.orbroker.binding.rule.impl.FactoryBindingRule;
import org.orbroker.binding.rule.impl.FieldBindingRuleImpl;
import org.orbroker.binding.rule.impl.FieldDelegateBinding;
import org.orbroker.binding.rule.impl.MethodBindingRule;
import org.orbroker.statement.CompoundStatement;
import org.orbroker.statement.Statement;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * NOT Thread safe
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Ingester extends DefaultHandler {
	private static final String COLUMN = "column";
	private static final String FROM = "from";
	private static final String AS = "as";
	private static final String TO = "to";
	private static final String RESULT = "result";
	private static final String IFEQUALS = "ifequals";
	private static final String WITH = "with";
	private static final String METHOD = "method";
	private static final String FACTORY = "factory";
	private static final String NAME = "name";
	private static final String CLASS = "class";
	private static final String ID = "id";

	private static final Logger logger = Logger.getLogger(Ingester.class);
	private final boolean TRACE = logger.isTraceEnabled();
	private StringBuilder indent = null;

	private Map<String, Statement> statements = new LinkedHashMap<String, Statement>();
	private Map<String, Binding> bindings = new LinkedHashMap<String, Binding>();

	private Stack instances = new Stack();
	private StringBuilder buffer = new StringBuilder();

	private Locator locator = null;

	enum Tag {
		statement, condition, conditions, content, append,
		binding, property, method, extend, column, delegate;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public Map<String, Statement> getStatements() {
		return statements;
	}

	public Map<String, Binding> getBindings() {
		return bindings;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		buffer.append(ch, start, length);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (TRACE){
			indent.deleteCharAt(indent.length() - 1);
			logger.trace(format("Line %d: %s</%s>", locator.getLineNumber(), indent, qName));
		}
		Tag tag = tagFor(qName);
		if (tag != null){
			switch (tag){
				case statement:
					Statement stmt2 = popInstanceOfType(Statement.class);
					if (stmt2.getContent() == null || stmt2.getContent().isEmpty()){
						String msg = format("Statement '%s' has no content", stmt2.getName());
						throw new SAXParseException(msg, locator);
					}
					statements.put(stmt2.getName(), stmt2);
					break;
				case content:
					Statement stmt1 = popInstanceOfType(Statement.class);
					stmt1.setContent(buffer.toString());
					instances.push(stmt1);
					break;
				case condition:
					String id = popInstanceOfType(String.class);
					Statement stmt = popInstanceOfType(Statement.class);
					CompoundStatement compoundStmt = (stmt instanceof CompoundStatement) ? (CompoundStatement) stmt :
							 				new CompoundStatement(stmt.getName(), stmt.getContent(), stmt.getBindingName());

					compoundStmt.addCondition(id, buffer.toString());
					instances.push(compoundStmt);
					break;
				case append:
					CompoundStatement compoundStmt1 = popInstanceOfType(CompoundStatement.class);
					compoundStmt1.setTail(buffer.toString());
					instances.push(compoundStmt1);
					break;
				case binding:
					Binding bnd = popInstanceOfType(Binding.class);
					bindings.put(bnd.getName(), bnd);
					break;
				case property:
				case method:
					BindingRule rule = popInstanceOfType(BindingRule.class);
					Binding binding1 = popInstanceOfType(Binding.class);
					binding1.addRule(rule);
					instances.push(binding1);
					break;
				case extend:
					ExtendsBindingRule ext = popInstanceOfType(ExtendsBindingRule.class);
					Binding binding2 = popInstanceOfType(Binding.class);
					binding2.addRule(ext);
					instances.push(binding2);
					break;
			}

			buffer.delete(0, buffer.length());
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (TRACE){
			if (indent == null) indent = new StringBuilder();
			logger.trace(format("Line %d: %s<%s>", locator.getLineNumber(), indent, qName));
			indent.append("\t");
		}
		Tag tag = tagFor(qName);
		if (tag != null){
			switch (tag){
				case statement:
						Statement stmt = null;
						String stmtName = getRequiredAttribute(attributes, ID);
						String bindingName = attributes.getValue(RESULT);
						stmt = new Statement(stmtName, null, bindingName);
						instances.push(stmt);
					break;
				case content:
					String parentStmtName1 = attributes.getValue(FROM);
					Statement parentStmt1 =  statements.get(parentStmtName1);
					if (parentStmt1 != null) {
						buffer.append(parentStmt1.getContent());
					}
					break;
				case condition:
						instances.push(getRequiredAttribute(attributes, ID));
					break;
				case conditions:
						String parentStmtName = getRequiredAttribute(attributes, FROM);
						Statement stmt1 = popInstanceOfType(Statement.class);
						if (!(stmt1 instanceof CompoundStatement)){
							stmt1 = new CompoundStatement(stmt1.getName(),
															stmt1.getContent(),
															stmt1.getBindingName());

						}
						Statement parentStmt = statements.get(parentStmtName);
						if (parentStmt == null || !(parentStmt instanceof CompoundStatement)){
							String msg = format("'%s' does not exist or has no 'conditions', cannot " +
									"be used by '%s' as parent", parentStmtName, stmt1.getName());
							throw new SAXParseException(msg, locator);
						}
						((CompoundStatement)stmt1).importConditionsFrom((CompoundStatement)parentStmt);
						instances.push(stmt1);
					break;
				case binding:
						Binding binding = new Binding(getRequiredAttribute(attributes, ID),
													forName(getRequiredAttribute(attributes, CLASS)));
						instances.push(binding);
					break;
				case property:
						String propName = getRequiredAttribute(attributes, NAME);
						String[] extraParams = getRequiredAttributes(attributes, FACTORY, METHOD);
						FieldBindingRuleImpl fieldRule = (extraParams[0] == null) ?
														new FieldBindingRuleImpl(propName):
														new FactoryBindingRule(propName, forName(extraParams[0]), extraParams[1]);
						instances.push(fieldRule);
					break;
				case column:
						String columnName = getRequiredAttribute(attributes, NAME);
						String columnType = attributes.getValue(AS);
						Column column = new Column(columnName, (columnType != null) ? forName(columnType) : null);
						BindingRule bindingRule = popInstanceOfType(BindingRule.class);
						bindingRule.addColumn(column);
						instances.push(bindingRule);
					break;
				case delegate:
						String to = getRequiredAttribute(attributes, TO);
						String columnType1 = attributes.getValue(AS);
						FieldBindingRuleImpl fieldRule1 = popInstanceOfType(FieldBindingRuleImpl.class);
						FieldDelegateBinding fieldDelegate = new FieldDelegateBinding(fieldRule1.getPropertyName(), getBinding(to));
						Class<?> destType = (columnType1 != null) ? forName(columnType1) : null;
						String[] condition = getRequiredAttributes(attributes, IFEQUALS, COLUMN);
						fieldDelegate.getColumn().setName(condition[1]);
						fieldDelegate.setCondition(condition[0]);
						fieldDelegate.getColumn().setDestinationType(destType);
						instances.push(fieldDelegate);
					break;
				case method:
						String methodName = getRequiredAttribute(attributes, NAME);
						MethodBindingRule methodRule = new MethodBindingRule(methodName);
						instances.push(methodRule);
					break;
				case extend:
						ExtendsBindingRule rule = new ExtendsBindingRule(
																getBinding(getRequiredAttribute(attributes, WITH)),
																getRequiredAttribute(attributes, IFEQUALS));
						instances.push(rule);
					break;

			}
		}
	}

	private Binding getBinding(String bindingName) throws SAXParseException {
		Binding binding = bindings.get(bindingName);
		if (binding == null){
			String msg = format("Unable to find binding with name '%s' among %s",
													bindingName, bindings.keySet());
			throw new SAXParseException(msg, locator);
		}
		return binding;
	}

	private Tag tagFor(String localName) {
		Tag tag = null;
		try{
			tag = Tag.valueOf(localName);
		} catch (IllegalArgumentException ex) { /* ignore */}
		return tag;
	}

	private String[] getRequiredAttributes(Attributes attributesHolder, String ... names) throws SAXParseException {
		String[] attributes = new String[names.length];
		boolean previousIsNull = false;
		for (int i = 0; i < names.length; i++){
			attributes[i] = attributesHolder.getValue(names[i]);
			boolean currentIsNull = attributes[i] == null;
			if ((i > 0) && (previousIsNull != currentIsNull)){
				String msg = format("Missing one of required attributes among %s",
												Arrays.asList(names));
				throw new SAXParseException(msg, locator);
			}
			previousIsNull = currentIsNull;
		}

		return attributes;
	}

	private String getRequiredAttribute(Attributes attributes, String name) throws SAXParseException {
		String attributeValue = attributes.getValue(name);
		if (attributeValue == null){
			String msg = format("Missing required attribute '%s'.", name);
			throw new SAXParseException(msg, locator);
		}
		return attributeValue;
	}

	private <T> T popInstanceOfType(Class<T> requiredType) throws SAXParseException {
		Object peek = null;
		try{
			peek = instances.peek();
		} catch (EmptyStackException e) {
			String msg = format("Expecting '%s', got null.", requiredType);
			throw new SAXParseException(msg, locator);
		}

		boolean instance = requiredType.isAssignableFrom(peek.getClass());
		if (!instance){
			String msg = format("Expected '%s', got '%s'", requiredType, peek.getClass());
			throw new SAXParseException(msg, locator);
		}
		return (T) instances.pop();
	}
}
