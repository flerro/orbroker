package org.orbroker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orbroker.Broker;
import org.orbroker.binding.Binding;
import org.orbroker.configuration.BrokerConfiguration;
import org.orbroker.configuration.BrokerConfigurationFactory;
import org.orbroker.statement.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import custom.datamodel.Company;
import custom.datamodel.Employee;
import custom.datamodel.Manager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"domain-context.xml"})
public class BrokerTest {

	private static final Logger logger = Logger.getLogger(BrokerTest.class);

	@Autowired
	Broker broker;

	@Autowired
	@Qualifier("localhost")
	DataSource dataSource;

	@Autowired
	@Qualifier("zikzak")
	Company zikzak;

	@Autowired
	@Qualifier("contactlab")
	Company contactlab;

	@Autowired
	@Qualifier("employee")
	Employee expectedEmployee;

	@Autowired
	@Qualifier("manager")
	Manager expectedManager;

	@Test
	public void configurationIngestion(){
		boolean fired = false;
		BrokerConfiguration cfg = null;
		try {
			ClassPathResource configurationSource = new ClassPathResource("orm/config.xml");
			cfg = BrokerConfigurationFactory.getFactory(configurationSource.getFile()).getBrokerConfiguration();
		} catch (Exception e) {
			fired = true;
			logger.error("Error running test", e);
		}

		assertFalse(fired);
		assertNotNull(cfg);

		Collection<Binding> bindings = cfg.getBindings();
		assertNotNull(bindings);
		assertEquals(3, bindings.size());

		// TODO check bindings and rule consistency with configuration

		Collection<Statement> statements = cfg.getStatements();
		assertNotNull(statements);
		assertEquals(13, statements.size());

		Map<String,String> declaredStatements = new HashMap<String,String>();
		declaredStatements.put("countCompany","SELECT count(id) FROM company WHERE id = :id");
		declaredStatements.put("selectCompany","SELECT c.* FROM company c");
		declaredStatements.put("selectCompanyById","SELECT c.* FROM company c  WHERE c.id = :id");
		declaredStatements.put("selectAllCompanies","SELECT c.* FROM company c");
		declaredStatements.put("selectEmployeeById","SELECT e.*, c.* as company_name, u.name as bu FROM employee e LEFT JOIN company c on c.id = e.company_id LEFT JOIN unit u on u.employee_id = e.id  WHERE e.id = :id  ORDER BY e.id DESC");
		declaredStatements.put("selectEmployee","SELECT e.*, c.* as company_name, u.name as bu FROM employee e LEFT JOIN company c on c.id = e.company_id LEFT JOIN unit u on u.employee_id = e.id");
		declaredStatements.put("selectEmployeeByCompanyId","SELECT e.*, c.* as company_name, u.name as bu FROM employee e LEFT JOIN company c on c.id = e.company_id LEFT JOIN unit u on u.employee_id = e.id  WHERE e.company_id = :id  ORDER BY e.id DESC");
		declaredStatements.put("countEmployeeByCompanyId","SELECT count(*) FROM employee e LEFT JOIN company c on c.id = e.company_id  WHERE e.company_id = :id");
		declaredStatements.put("countEmployee","SELECT count(*) FROM employee e LEFT JOIN company c on c.id = e.company_id");
		declaredStatements.put("countEmployeeById","SELECT count(*) FROM employee e LEFT JOIN company c on c.id = e.company_id  WHERE e.id = :id");
		declaredStatements.put("countCompanies"," SELECT count(id) FROM company");
		declaredStatements.put("insertCompany","INSERT INTO company( #foreach($f in $fields) `$f` #if( $velocityHasNext ),#end #end ) VALUES ( #foreach($f in $fields) :$f #if( $velocityHasNext ),#end #end )");
		declaredStatements.put("deleteCompany", "DELETE FROM company WHERE id = :id");

		for (Statement stmt : statements){
			String content = declaredStatements.get(stmt.getName());
			assertNotNull(content);
			assertEquals(content.trim(), stmt.getContent().trim());
		}
	}

	@Test
	public void simplePropertyAndMethodBinding(){

		SqlParameterSource params1 = new MapSqlParameterSource("id", 1);
		Company company = broker.<Company>fetchOne("selectCompanyById", params1, dataSource);
		assertEquals(contactlab.getId(), company.getId());
		assertEquals(contactlab.getName(), company.getName());
		assertEquals(contactlab.getAddress(), company.getAddress());

		SqlParameterSource params2 = new MapSqlParameterSource("id", 2);
		company = broker.<Company>fetchOne("selectCompanyById", params2, dataSource);
		assertEquals(zikzak.getId(), company.getId());
		assertEquals(zikzak.getName(), company.getName());
		assertEquals(zikzak.getAddress(), company.getAddress());
	}

	@Test
	public void delegatePropertyBinding(){

		SqlParameterSource params = new MapSqlParameterSource("id", 1);
		Employee employee = broker.<Employee>fetchOne("selectEmployeeById", params, dataSource);

		assertNotNull(employee);
		assertEquals(expectedEmployee.getName(), employee.getName());
		assertEquals(expectedEmployee.getEmail(), employee.getEmail());
		assertEquals(expectedEmployee.getActive(), employee.getActive());
		assertEquals(expectedEmployee.getBirthDate(), employee.getBirthDate());
		assertEquals(expectedEmployee.getType(), employee.getType());
		assertEquals(expectedEmployee.getCompany().getName(), employee.getCompany().getName());
		assertEquals(expectedEmployee.getCompany().getAddress(), employee.getCompany().getAddress());
	}

	@Test
	public void inheritBinding(){

		SqlParameterSource params = new MapSqlParameterSource("id", 2);
		Manager manager = broker.<Manager>fetchOne("selectEmployeeById", params, dataSource);

		assertNotNull(manager);
		assertEquals(expectedManager.getName(), manager.getName());
		assertEquals(expectedManager.getEmail(), manager.getEmail());
		assertEquals(expectedManager.getActive(), manager.getActive());
		assertEquals(expectedManager.getBirthDate(), manager.getBirthDate());
		assertEquals(expectedManager.getType(), manager.getType());
		assertEquals(expectedManager.getBusinessUnit(), manager.getBusinessUnit());
		assertEquals(expectedManager.getCompany().getName(), manager.getCompany().getName());
		assertEquals(expectedManager.getCompany().getAddress(), manager.getCompany().getAddress());
	}

	@Test
	public void countCompanies(){
		Long expectedCount = 2L;

		Long count = broker.<Long>fetchOne("countCompanies", dataSource);
		assertNotNull(count);
		assertEquals(expectedCount, count);
	}

	@Test
	public void dynamicStatementParsing(){
		Map<String, Object> fieldNameAndValues = new LinkedHashMap<String, Object>();
		fieldNameAndValues.put("company_name", "test");
		fieldNameAndValues.put("address", "test");

		SqlParameterSource fields = new MapSqlParameterSource(fieldNameAndValues);
		Map<String, Object> objs = new LinkedHashMap<String, Object>();
		objs.put("fields", fieldNameAndValues.keySet());
		Long pk = broker.insert("insertCompany", fields, objs, dataSource);
		assertNotNull(pk);

		SqlParameterSource deleteParams = new MapSqlParameterSource("id", pk);
		broker.execute("deleteCompany", deleteParams, dataSource);
	}

	@Test
	public void multipleSelection(){
		List<Company> companies = broker.<Company>fetch("selectCompany", dataSource);
		int expectedCount = 2;

		assertNotNull(companies);
		assertTrue(expectedCount <= companies.size());

		List<Employee> employees = broker.<Employee>fetch("selectEmployee", dataSource);
		assertNotNull(employees);
		assertTrue(expectedCount <= companies.size());
	}

}
