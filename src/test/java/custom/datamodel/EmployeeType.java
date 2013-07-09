package custom.datamodel;

public enum EmployeeType {
	MANAGER,
	STANDARD;

	public static EmployeeType valueOf(Integer i){
		switch (i){
			case 0: return MANAGER;
			case 1: return STANDARD;
			default:
				throw new IllegalArgumentException("Unable to translate " + i + "to an EmployeeType");
		}
	}
}
