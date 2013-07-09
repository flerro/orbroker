package org.orbroker.binding;

public class Column {

	 private String name;
	 private Class<?> destinationType;
	
	 public Column(String name, Class<?> destinationType) {
		this.name = name;
		this.destinationType = destinationType;
	 }

	 public Column(String name) {
		this.name = name;
		this.destinationType = null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getDestinationType() {
		return destinationType;
	}

	public void setDestinationType(Class<?> destinationType) {
		this.destinationType = destinationType;
	}

	public boolean hasDestinationType(){
		return destinationType != null;
	}

	@Override
	public String toString() {
		return name;
	}

}
