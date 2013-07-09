package custom.datamodel;

public class Company {
	private Long id;
	private String name;
	private String address;
	
	public void setAddress(String addr){
		this.address = addr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
}
