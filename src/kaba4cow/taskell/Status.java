package kaba4cow.taskell;

public enum Status {

	PROGRESS("In progress"), NONE("None       "), DONE("Done       ");

	private final String name;

	private Status(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
