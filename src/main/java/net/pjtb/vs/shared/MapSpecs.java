package net.pjtb.vs.shared;

public class MapSpecs extends Specs<MapSpecs> {
	private final int id;

	protected MapSpecs(int id) {
		setSelf(this);
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
