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
	//TODO: instead of using two bytes, we can just use a single short
	//to represent a channel number in a particular world, e.g. (world * 100 + channel)
	private final Map<String, Handler<Message<Buffer>>> handlers = new HashMap<>();
	private final Map<Byte, Map<Byte, Map<Integer, ChannelMap>>> loadedMaps = new HashMap<>();

	/**
	 * If loadedMaps does not contain entries for the world and channel,
	 * then this method will create them.
	 *
	 * @param worldObj
	 * @param channelObj
	 * @param idObj
	 * @return null if the map is not yet loaded.
	 */
	private ChannelMap getMap(Byte worldObj, Byte channelObj, Integer idObj) {
		Map<Byte, Map<Integer, ChannelMap>> worldMaps = loadedMaps.get(worldObj);
		if (worldMaps == null) {
			worldMaps = new HashMap<>();
			loadedMaps.put(worldObj, worldMaps);
			return null;
		}

		Map<Integer, ChannelMap> channelMaps = worldMaps.get(channelObj);
		if (channelMaps == null) {
			channelMaps = new HashMap<>();
			worldMaps.put(worldObj, channelMaps);
			return null;
		}

		return channelMaps.get(idObj);
	}

	private void listenForMapSpecificRequests() {
		JsonObject config = container.config();
		byte startChannel = (byte) ((Integer) config.getNumber("startChannel")).intValue();
		byte endChannel = (byte) ((Integer) config.getNumber("endChannel")).intValue();
		byte world = (byte) ((Integer) config.getNumber("world")).intValue();
		Byte worldObj = Byte.valueOf((byte) world);
		for (byte i = startChannel; i < endChannel; i++) {
			byte channel = i;
			Byte channelObj = Byte.valueOf((byte) channel);
			String address = String.format(EventAddresses.MAP_REQUEST, worldObj, channelObj);
			Handler<Message<Buffer>> requestHandler = msg -> {
				Map<String, Object> rxBson = BSON.decode(msg.body());
				String op = (String) rxBson.get("op");
				Integer mapId = (Integer) rxBson.get("id");

				ChannelMap map = getMap(worldObj, channelObj, mapId);
				switch (op) {
					case "ensureLoaded": {
						if (map != null) {
							msg.reply();
						} else {
							MapSpecsFactory.getInstance().retrieveSpecs(mapId.intValue(), specs -> {
								if (specs != null) {
									loadedMaps.get(worldObj).get(channelObj).put(mapId, new ChannelMap(vertx, world, channel, mapId.intValue(), specs));
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
			for (Map<Byte, Map<Integer, ChannelMap>> worldMaps : loadedMaps.values())
				for (Map<Integer, ChannelMap> channelMaps : worldMaps.values())
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
