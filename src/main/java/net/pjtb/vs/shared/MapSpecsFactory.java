package net.pjtb.vs.shared;

import java.util.HashMap;
import java.util.Map;

import net.pjtb.vs.specsside.SpecsLoaderDaemon;

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
		txBson.put("id", Integer.valueOf(populate.getId()));
		eventBus.sendWithTimeout(SpecsLoaderDaemon.class.getName() + ".map", BSON.encode(txBson), TIMEOUT, result -> populate.setLoaded(result.succeeded()));
	}

	public void retrieveSpecs(int mapId, Handler<MapSpecs> doneHandler, EventBus eventBus) {
		super.retrieveSpecs(Integer.valueOf(mapId), doneHandler, eventBus);
	}

	public MapSpecs getSpecsShell(int mapId) {
		return super.getSpecsShell(Integer.valueOf(mapId));
	}
}
