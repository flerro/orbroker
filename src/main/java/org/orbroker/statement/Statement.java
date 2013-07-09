package org.orbroker.statement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Statement {
	protected String name;
	protected String content;
	protected String bindingName;

	public Statement(){}

	public Statement(String name, String content, String bindingName) {
		this.name = name;
		this.content = purify(content);
		this.bindingName = bindingName;
	}

	public void update(String name, String content, String bindingName) {
		this.name = name;
		this.content = purify(content);
		this.bindingName = bindingName;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = purify(content);
	}
	public String getBindingName() {
		return bindingName;
	}
	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;
	}

	public Statement copy(){
		return new Statement(this.getName(), this.getContent(), this.getBindingName());
	}

	protected final String purify(String content) {
		if (content == null){
			return null;
		}

		StringBuffer buffer = new StringBuffer();
		Pattern pattern = Pattern.compile("([\\n\\t])+");
		Matcher matcher = pattern.matcher(Pattern.quote(content));
		while (matcher.find()) {
			matcher.appendReplacement(buffer, " ");
		}
		matcher.appendTail(buffer);

		return (content != null) ? content.replaceAll("([\\n\\t])+", " ") : null;
	}

	@Override
	public String toString() {
		return "SqlStatement [" +
					"name=" + name + ", " +
					"content=" + content + ", " +
					"bindingName=" + bindingName
				+ "]";
	}
}
