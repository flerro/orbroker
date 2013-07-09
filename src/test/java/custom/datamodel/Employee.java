package custom.datamodel;

import java.util.Date;

public class Employee {
	private String name;
	private String email;
	private String salary;
	private Boolean active;
	private Date birthDate;
	private EmployeeType type;
	private Company company;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(java.sql.Date birthDate) {
		this.birthDate = birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public void setSalary(Integer amount, String currency) {
		this.salary = amount + " " + currency;
	}
	public String getSalary() {
		return this.salary;
	}

	public EmployeeType getType() {
		return type;
	}
	public void setType(EmployeeType type) {
		this.type = type;
	}
	public void setSalary(String salary) {
		this.salary = salary;
	}
	public Company getCompany() {
		return company;
	}
	public void setCompany(Company campaign) {
		this.company = campaign;
	}

}
