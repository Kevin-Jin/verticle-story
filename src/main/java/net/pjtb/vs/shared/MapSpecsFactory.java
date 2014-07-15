package net.pjtb.vs.shared;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;

import com.jetdrone.vertx.mods.bson.BSON;

public class MapSpecsFactory extends SpecsFactory<Integer, MapSpecs> {
	private static final int TIMEOUT = 10000;

	private static final MapSpecsFactory instance = new MapSpecsFactory();

	public static MapSpecsFactory getInstance() {
		return instance;
	}

	private MapSpecsFactory() {
		
	}

	@Override
	protected MapSpecs specsShell(Integer mapId) {
		return new MapSpecs(mapId.intValue());
	}

	@Override
	protected void loadAsync(MapSpecs populate, EventBus eventBus) {
		Map<String, Object> txBson = new HashMap<String, Object>();
		txBson.put("id", EventAddresses.mapKey(populate.getId()));
		eventBus.sendWithTimeout(String.format(EventAddresses.SPECS_LOADED_NOTIFICATION, "map"), BSON.encode(txBson), TIMEOUT, result -> populate.setLoaded(result.succeeded()));
	}

	public void retrieveSpecs(int mapId, Handler<MapSpecs> doneHandler, EventBus eventBus) {
		super.retrieveSpecs(EventAddresses.mapKey(mapId), doneHandler, eventBus);
	}

	public MapSpecs getSpecsShell(int mapId) {
		return super.getSpecsShell(EventAddresses.mapKey(mapId));
	}
}
