package net.pjtb.vs.mapside;

import java.util.HashMap;
import java.util.Map;

import net.pjtb.vs.shared.EventAddresses;
import net.pjtb.vs.shared.MapSpecsFactory;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import com.jetdrone.vertx.mods.bson.BSON;

public class MapDaemon extends Verticle {
	private final Map<String, Handler<Message<Buffer>>> handlers = new HashMap<>();
	private final Map<Short, Map<Integer, ChannelMap>> loadedMaps = new HashMap<>();

	/**
	 * If loadedMaps does not contain entries for the channel,
	 * then this method will create them.
	 *
	 * @param fqChKey
	 * @param idKey
	 * @return null if the map is not yet loaded.
	 */
	private ChannelMap getMap(Short fqChKey, Integer idKey) {
		Map<Integer, ChannelMap> channelMaps = loadedMaps.get(fqChKey);
		if (channelMaps == null) {
			channelMaps = new HashMap<>();
			loadedMaps.put(fqChKey, channelMaps);
			return null;
		}

		return channelMaps.get(idKey);
	}

	private void listenForMapSpecificRequests() {
		JsonObject config = container.config();
		byte startChannel = ((Number) config.getNumber("startChannel")).byteValue();
		byte endChannel = ((Number) config.getNumber("endChannel")).byteValue();
		byte world = ((Number) config.getNumber("world")).byteValue();
		for (byte i = startChannel; i < endChannel; i++) {
			byte channel = i;
			Short fqChKey = EventAddresses.fullyQualifiedChannelKey(world, channel);
			String address = String.format(EventAddresses.MAP_REQUEST, fqChKey);
			Handler<Message<Buffer>> requestHandler = msg -> {
				Map<String, Object> rxBson = BSON.decode(msg.body());
				String op = (String) rxBson.get("op");
				Integer mapId = (Integer) rxBson.get("id");

				ChannelMap map = getMap(fqChKey, mapId);
				switch (op) {
					case "ensureLoaded": {
						if (map != null) {
							msg.reply();
						} else {
							MapSpecsFactory.getInstance().retrieveSpecs(mapId.intValue(), specs -> {
								if (specs != null) {
									loadedMaps.get(fqChKey).put(mapId, new ChannelMap(vertx, world, channel, mapId.intValue(), specs));
									msg.reply();
								} else {
									msg.fail(1, "Failed to load map specifications");
								}
							}, vertx.eventBus());
						}
						break;
					}
					case "spawn": {
						if (map == null) {
							msg.fail(1, "Map is not loaded");
							return;
						}
						int mobId = ((Integer) rxBson.get("mob")).intValue();
						map.spawn(mobId);
						msg.reply();
						break;
					}
				}
			};
			vertx.eventBus().registerHandler(address, requestHandler);
			handlers.put(address, requestHandler);
		}
	}

	private void setUpRespawns() {
		vertx.setPeriodic(30000, timer -> {
			for (Map<Integer, ChannelMap> channelMaps : loadedMaps.values())
				for (ChannelMap map : channelMaps.values())
					map.respawn();
		});
	}

	@Override
	public void start() {
		listenForMapSpecificRequests();
		setUpRespawns();
	}
}
