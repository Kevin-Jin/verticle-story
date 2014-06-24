package net.pjtb.vs.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vertx.java.core.Handler;

public abstract class Specs<T extends Specs<T>> {
	private final Object monitor;
	private T self;
	private List<Handler<T>> listeners;
	private volatile boolean loaded, success;

	protected Specs() {
		monitor = new Object();
		loaded = false;
		listeners = new ArrayList<>();
	}

	protected void setSelf(T self) {
		this.self = self;
	}

	protected void setLoaded(boolean success) {
		assert !loaded;

		List<Handler<T>> listeners;
		synchronized (monitor) {
			listeners = this.listeners;
			this.listeners = Collections.emptyList(); 
			loaded = true;
			this.success = success;
		}
		if (success)
			for (Handler<T> doneHandler : listeners)
				doneHandler.handle(self);
		else
			for (Handler<T> doneHandler : listeners)
				doneHandler.handle(null);
	}

	protected void addOnLoaded(Handler<T> doneHandler) {
		//once loaded is true, it will never go back to false.
		//even if double checked locking fails and we incorrectly go into the synchronized block,
		//no harm will be done because by the end, we'll see that loaded is in fact true.
		//thus, double checked locking enables us to safely avoid synchronization when possible.
		boolean loaded = this.loaded;
		if (!loaded) {
			synchronized (monitor) {
				loaded = this.loaded;
				if (!loaded)
					listeners.add(doneHandler);
			}
		}
		if (loaded)
			if (success)
				doneHandler.handle(self);
			else
				doneHandler.handle(null);
	}

	public boolean isLoaded() {
		return loaded;
	}
}
