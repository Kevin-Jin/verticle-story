package net.pjtb.vs.playerside;

import java.util.HashMap;
import java.util.Map;

import net.pjtb.vs.shared.EventAddresses;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import com.jetdrone.vertx.mods.bson.BSON;

public class PartyFactory {
	private static final int TIMEOUT = 1000;

	//keeping a single copy of the Party for each thread is more efficient than a copy for each WorldCharacter.
	//we use a copy of the Party per thread instead of per instance because the overhead of synchronization
	//may overweigh the space utilization benefits.
	private static final ThreadLocal<PartyFactory> INSTANCE = new ThreadLocal<PartyFactory>() {
		@Override
		protected PartyFactory initialValue() {
			return new PartyFactory();
		}
	};

	public static PartyFactory getInstance() {
		return INSTANCE.get();
	}

	private final Map<Byte, Map<Integer, Party>> loadedParties;

	private PartyFactory() {
		loadedParties = new HashMap<>();
	}

	private void registerWorld(EventBus eventBus, Byte worldObj) {
		eventBus.registerHandler(String.format(EventAddresses.PARTY_REQUEST, worldObj), (Handler<Message<Buffer>>) (Message<Buffer> msg) -> {
			Map<String, Object> rxBson = BSON.decode(msg.body());
			String op = (String) rxBson.get("op");
			Integer partyId = (Integer) rxBson.get("id");

			switch (op) {
				case "load": {
					retrieveParty(worldObj.byteValue(), partyId.intValue(), eventBus, party -> {
						if (party == null) {
							msg.fail(1, "Failed to load party");
							return;
						}

						//TODO: add party information to txBson
						Map<String, Object> txBson = new HashMap<>();
						msg.reply(BSON.encode(txBson));
					});
					break;
				}
			}
		});
	}

	public void retrieveParty(byte world, int id, EventBus eventBus, Handler<Party> doneHandler) {
		Byte worldObj = Byte.valueOf(world);
		Integer idObj = Integer.valueOf(id);
		Map<Integer, Party> worldParties = loadedParties.get(worldObj);
		boolean registerWorld = false;
		if (worldParties == null) {
			registerWorld = true;
			worldParties = new HashMap<>();
			loadedParties.put(worldObj, worldParties);
		}

		Party p = worldParties.get(idObj);
		if (p != null) {
			p.addOnLoaded(doneHandler);
		} else {
			Party newParty = new Party();
			worldParties.put(idObj, newParty);
			Map<String, Object> txBson = new HashMap<>();
			txBson.put("op", "load");
			txBson.put("id", idObj);

			eventBus.sendWithTimeout(String.format(EventAddresses.PARTY_REQUEST, worldObj), txBson, TIMEOUT, (AsyncResult<Message<Buffer>> result) -> {
				//send a request to other threads to obtain party info
				if (result.succeeded()) {
					Map<String, Object> rxBson = BSON.decode(result.result().body());
					// TODO: load party information from rxBson
					newParty.setLoaded(true);
				} else {
					//no one else has the party
					//TODO: load party information from database
					newParty.setLoaded(true);
				}
				doneHandler.handle(newParty);
			});
			if (registerWorld)
				//if we haven't already, register a party request handler for this world
				registerWorld(eventBus, worldObj);
		}
	}
}
