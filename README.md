OR-Broker is yet another library trying to solve the mapping problem between Java objects and tables in a relational database.

The reason for it?

> I had the need to tackle a big and legacy (un-touchable) DB schema but I didn't want to mirror it in the application data-model. I wanted total freedom in my data-model: binding factory or multi params mehtods, handling inheritance. Moreover I wanted total control over the executed SQL statemets (on multiple datasources), and I did want to keep my SQL DRY.

## Know it

Build your own query in plain old SQL (wrapped in an XML envelope)

```XML
<statement id="selectCompany" result="company">
    <content>
    SELECT c.*
    FROM company c
    </content>
    <condition id="ById">
    WHERE c.id = :id
    </condition>
</statement>
```

Configure the data binding using properties or methods of your data-model

```XML
<binding id="company" class="custom.datamodel.Company">
	<property name="id">
		<!-- Explicit type convertion -->
		<column name="id" as="java.lang.Long"/>
	</property>
	<!-- set property -->
	<property name="name">
		<column name="company_name"/>
	</property>
	<!-- bind column by property name -->
	<property name="address"/>
	<!--
	Alternative method syntax ia available
	<method name="setAddress">
		<column name="address" />
	</method>
	-->
</binding>
```

Perform queries using the library API

```Java
Map params = new HashMap();
params.put("id", 1);
Company company =
        broker.fetchOne("selectCompanyById", params, dataSource);
```

The mapping will be performed by the OR-broker using _reflection_.


### Keep your SQL DRY

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
	<!-- multiple WHEREs -->
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

and use them accordingly

```Java
// Call the 'selectEmployee' statement using the 'ById' condition
Employee employee =
    broker.<Employee>fetchOne("selectEmployeeById", params, dataSource);
```

Share ``WHERE`` conditions between statements

```XML
<statement id="countEmployee" result="company">
	<content>
	SELECT count(*)
	FROM employee e
	LEFT JOIN company c on c.id = e.company_id
	</content>
	<!-- inherits where conditions from the
			selectEmployee statements -->
	<conditions from="selectEmployee"/>
</statement>
```

to avoid duplications in SQL.

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

to build repetitive SQL queries.

### Freedom for objects

Bind DB tables and your data-model using multi-parameter methods, call factory methods, deal with inheritance

```XML
<binding id="employee" class="custom.datamodel.Employee">
	<property name="name"/>
	<property name="email">
		<column name="email"/>
	</property>
	<!-- Call the given factory method
		to value the 'type' property (it is an Enum type)-->
	<property name="type" factory="custom.datamodel.EmployeeType" method="valueOf">
		<column name="type"/>
	</property>
	...
	<!-- call a method using two TABLE columns as parameters -->
	<method name="setSalary">
		<column name="salary" />
		<column name="currency"/>
	</method>
	<!-- Return an instance of Manager (Manager extends Employee)
			if column named type has value 0 -->
	<extend with="manager" ifequals="0">
		<column name="type" />
	</extend>
</binding>
```

Write your own adapter for seamless type conversion between DB columns types and your classes

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

they will loaded and _auto-magically_ applied when a data-type conversion is needed.

```XML
<binding id="employee" class="custom.datamodel.Employee">
	...
	<!-- JDBC type for the 'birth' colunm is java.sql.Timestamp,
			Employee.birtDate property type is java.util.Date-->
	<property name="birthDate">
		<column name="birth"/>
	</property>
	...
</binding>
```

as in the example above.


# Use it
It's a [Maven](http://maven.apache.org/) project.
Just download it to your machine and install into your maven repository

```bash
 $ wget -O orbroker.zip https://github.com/flerro/orbroker/archive/master.zip
 $ unzip orbroker.zip
 $ cd orbroker-master
 orbroker-master/ $ mvn install
```

and add a reference to OR-broker as a dependency

```XML
   <dependency>
      <groupId>org.orbroker</groupId>
      <artifactId>orbroker</artifactId>
      <version>1.0</version>
   </dependency>
```

in your project ```pom.xml```.

## Behind the scenes

OR-Broker leverages:
 - Spring reflection utils for binding
 - velocity for optional SQL templating
 - standard Java XML SAX parser

If you are not familiar with maven please take a look to: [Maven in five minutes](http://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

## Status
Is it production ready? It is quite solid, please take a look at it and decide by yourself. It has been built (and used) to power a big API project handling lots of traffic.

## Known issues/drawbacks
- too much freedom means lots of XML boilerplate
- documentation is missing (please take a look at the tests for examples)
- XML is quite verbose (a DSL would be way better)

More info [here](http://www.rolandfg.net/2013/07/08/or-broker/)