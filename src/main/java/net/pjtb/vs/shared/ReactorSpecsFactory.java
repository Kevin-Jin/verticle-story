package net.pjtb.vs.shared;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;

import com.jetdrone.vertx.mods.bson.BSON;

public class ReactorSpecsFactory extends SpecsFactory<String, ReactorSpecs> {
	private static final int TIMEOUT = 10000;

	private static final ReactorSpecsFactory instance = new ReactorSpecsFactory();

	public static ReactorSpecsFactory getInstance() {
		return instance;
	}

	private ReactorSpecsFactory() {
		
	}

	@Override
	protected ReactorSpecs specsShell(String scriptName) {
		return new ReactorSpecs(scriptName);
	}

	@Override
	protected void loadAsync(ReactorSpecs populate, EventBus eventBus) {
		Map<String, Object> txBson = new HashMap<String, Object>();
		txBson.put("scriptName", populate.getScriptName());
		eventBus.sendWithTimeout(String.format(EventAddresses.SPECS_LOADED_NOTIFICATION, "reactor"), BSON.encode(txBson), TIMEOUT, result -> populate.setLoaded(result.succeeded()));
	}

	@Override
	public void retrieveSpecs(String scriptName, Handler<ReactorSpecs> doneHandler, EventBus eventBus) {
		super.retrieveSpecs(scriptName, doneHandler, eventBus);
	}
}
