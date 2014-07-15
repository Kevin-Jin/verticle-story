package net.pjtb.vs.mapside;

import net.pjtb.vs.shared.EventAddresses;
import net.pjtb.vs.shared.MapSpecs;

import org.vertx.java.core.Vertx;

public class ChannelMap {
	private final Vertx vertx;
	private final byte world, channel;
	private final int mapId;
	private final MapSpecs specs;

	protected ChannelMap(Vertx vertx, byte world, byte channel, int id, MapSpecs specs) {
		this.vertx = vertx;
		this.world = world;
		this.channel = channel;
		this.mapId = id;
		this.specs = specs;
	}

	protected void spawn(int mobId) {
		//send packets for monster spawns
		vertx.eventBus().publish(String.format(EventAddresses.MAP_BROADCAST, EventAddresses.fullyQualifiedChannelKey(world,  channel), EventAddresses.mapKey(mapId)), new byte[0]);
	}

	protected void respawn() {
		//TODO: get monsters list from specs
		spawn(2324334);
	}
}
