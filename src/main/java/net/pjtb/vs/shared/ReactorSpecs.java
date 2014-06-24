package net.pjtb.vs.shared;

public class ReactorSpecs extends Specs<ReactorSpecs> {
	private final String scriptName;

	protected ReactorSpecs(String scriptName) {
		setSelf(this);
		this.scriptName = scriptName;
	}

	public String getScriptName() {
		return scriptName;
	}
}
