package camel.model;

public class ModelB {

	private String identifier;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "ModelB{" +
				"identifier='" + identifier + '\'' +
				'}';
	}
}
