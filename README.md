
OR-Broker is yet another library that would like to help solving the object-relational mapping problem. 

The reason for it?

> I had the need to tackle a big and legacy (un-touchable) DB schema but I didn't want to mirror it in the application data-model. I wanted total freedom in my data-model: binding factory or multi params mehtods, handling inheritance. Moreover I wanted total control over the executed SQL statemts, working in parallel on multiple datasources, and I did want to keep my SQL DRY. 

## Know it

Build your own query in plain old SQL (wrapped in an XML envelope)

```XML
<statement id="countCompany">
	<content>
	SELECT count(id)
	FROM company
	WHERE id = :id
	</content>
</statement>
```

and bind results with properties or method of your data-model

```XML
<binding id="company" class="custom.datamodel.Company">
	<property name="id">
		<column name="id" as="java.lang.Long"/>
	</property>
	<property name="name">
		<column name="company_name"/>
	</property>
	<method name="setAddress">
		<column name="address" />
	</method>
</binding>
```

### Keep your SQL DRY: 

Add multiple ``WHERE`` in one SQL statements 

```XML
<statement id="selectEmployee" result="employee">
	<content>
	SELECT
		e.*,
		c.* as company_name,
		u.name as bu
	FROM employee e
	LEFT JOIN company c on c.id = e.company_id
	LEFT JOIN unit u on u.employee_id = e.id
	</content>
	<condition id="ByCompanyId">
	WHERE e.company_id = :id
	</condition>
	<condition id="ById">
	WHERE e.id = :id
	</condition>
	<append>
	ORDER BY e.id DESC
	</append>
</statement>
```

And use them accordingly

```Java
Employee employee = broker.<Employee>fetchOne("selectEmployeeById", params, dataSource);
```

Share ``WHERE`` conditions between statements

```XML
<statement id="countEmployee" result="company">
	<content>
	SELECT count(*)
	FROM employee e
	LEFT JOIN company c on c.id = e.company_id
	</content>
	<conditions from="selectEmployee"/>
</statement>
```

Use Velocity templating in your statements

```XML
<statement id="insertCompany">
	<content>
	INSERT INTO company(
		#foreach($f in $fields)
			`$f` #if( $velocityHasNext ),#end
		#end
	) VALUES (
		#foreach($f in $fields)
			:$f #if( $velocityHasNext ),#end
		#end
	)
	</content>
</statement>
```

### Freedom for objects 

Bind multi parameter methods, call factory methods, work with inheritance

```XML
<binding id="employee" class="custom.datamodel.Employee">
	<property name="name"/>
	<property name="email">
		<column name="email"/>
	</property>
	<property name="type" factory="custom.datamodel.EmployeeType" method="valueOf">
		<column name="type"/>
	</property>
	...
	<method name="setSalary">
		<column name="salary" />
		<column name="currency"/>
	</method>
	<extend with="manager" ifequals="0">
		<column name="type" />
	</extend>
</binding>
```

Write your own adapter for seamless type convertion from DB column type to your model:

```Java
package org.orbroker.binding.adapter.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.orbroker.binding.adapter.BindingAdapter;

public class TimestampToDateAdapter implements BindingAdapter<Timestamp, Date> {
	@Override
	public Class<Timestamp> from() {
		return Timestamp.class;
	}

	@Override
	public Class<Date> to() {
		return Date.class;
	}

	@Override
	public Date valueOf(Object from) {
		return new Date(((Timestamp)from).getTime());
	}
}
```

```XML
<binding id="employee" class="custom.datamodel.Employee">
	...
	<property name="birthDate">
		<column name="birth"/>
	</property>
	...
</binding>
```
 
# Use it
It's a [Maven](http://maven.apache.org/) project. 

## Behind the scenes

OR-Broker leverages: 
 - Spring reflection utils for binding
 - velocity for optional SQL templating
 - standard Java XML SAX parser

If you are not familiar with maven please take a look to: [Maven in five minutes](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

## Status
Is it production ready? It is quite solid, please take a look at it and decide by yourself. It has been built (and used) to power a big API project handling lots of traffic.

## Known issues/drawbacks
- too much freedom means no convention over configuration (lots of XML boilerplate)
- documentation is missing (please take a look at the testcases under the test source folder)
- XML is quite verbose (a DSL would be way better)

# Thanks
Feel free to fork OR-Broker, I'm looking forward to your pull requests and to your feedback.

