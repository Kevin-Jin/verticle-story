package net.pjtb.vs.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;

/**
 * Handles the caching of specifications that can be safely used across a vert.x instance.
 *
 * @author Kevin
 */
public abstract class SpecsFactory<K, T extends Specs<T>> {
	//can be accessed by any thread on the instance so it needs to be thread safe
	private final ConcurrentMap<K, T> hits;

	protected SpecsFactory() {
		hits = new ConcurrentHashMap<>();
	}

	protected abstract T specsShell(K key);

	/**
	 * Use a Vert.x optimized database driver or run this operation on a worker verticle.
	 * It must not block. 
	 *
	 * @param populate
	 * @param eventBus
	 */
	protected abstract void loadAsync(T populate, EventBus eventBus);

	protected void retrieveSpecs(K key, Handler<T> doneHandler, EventBus eventBus) {
		T newData = specsShell(key);
		T existingData = hits.putIfAbsent(key, newData);
		if (existingData == null) {
			existingData = newData;
			existingData.addOnLoaded(doneHandler);
			loadAsync(newData, eventBus);
		} else {
			existingData.addOnLoaded(doneHandler);
		}
	}

	public T getSpecsShell(K key) {
		return hits.get(key);
	}
}
