package net.pjtb.vs.playerside;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vertx.java.core.Handler;

public class Party {
	private boolean loaded, success;
	private List<Handler<Party>> listeners;

	protected Party() {
		loaded = false;
		listeners = new ArrayList<>();
	}

	protected void setLoaded(boolean success) {
		loaded = true;
		this.success = success;

		//propagate to others who pinged us
		for (Handler<Party> doneHandler : listeners)
			doneHandler.handle(this);
		listeners = Collections.emptyList();
	}

	protected void addOnLoaded(Handler<Party> doneHandler) {
		if (!loaded)
			listeners.add(doneHandler);
		else if (success)
			doneHandler.handle(this);
		else
			doneHandler.handle(null);
	}
}
