package net.pjtb.vs.playerside;

import net.pjtb.vs.shared.EventAddresses;

public class WorldCharacter {
	private int charId;
	private ClientSession ses;
	private byte world, channel;
	private int map;
	private int party;
	private int guild;
	private int chatroom;

	public void init() {
		int mapFromDb = 0;
		int partyFromDb = 0;
		int guildFromDB = 0;
		int chatroomFromDb = 0;
		setMap(mapFromDb);
		setParty(partyFromDb);
		setGuild(guildFromDB);
		setChatroom(chatroomFromDb);
	}

	public int getId() {
		return charId;
	}

	public void setMap(int mapId) {
		Short fqChKey = EventAddresses.fullyQualifiedChannelKey(world, channel);
		Integer mapKey = EventAddresses.mapKey(map);
		ses.unsubscribe(String.format(EventAddresses.MAP_BROADCAST, fqChKey, mapKey));
		map = mapId;
		mapKey = EventAddresses.mapKey(map);
		ses.subscribe(String.format(EventAddresses.MAP_BROADCAST, fqChKey, mapKey));
		byte[] loginNotification = new byte[0];
		ses.publish(String.format(EventAddresses.MAP_BROADCAST, fqChKey, mapKey), loginNotification, charId);
	}

	public void setParty(int partyId) {
		Byte worldKey = EventAddresses.worldKey(world);
		Integer partyKey = EventAddresses.partyKey(party);
		if (party != 0)
			ses.unsubscribe(String.format(EventAddresses.PARTY_BROADCAST, worldKey, partyKey));
		party = partyId;
		if (party != 0) {
			partyKey = EventAddresses.partyKey(party);
			ses.subscribe(String.format(EventAddresses.PARTY_BROADCAST, worldKey, partyKey));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.PARTY_BROADCAST, worldKey, partyKey), loginNotification, charId);
		}
	}

	public void setGuild(int guildId) {
		Byte worldKey = EventAddresses.worldKey(world);
		guild = guildId;
		if (guild != 0) {
			Integer guildKey = EventAddresses.guildKey(guild);
			ses.subscribe(String.format(EventAddresses.GUILD_BROADCAST, worldKey, guildKey));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.GUILD_BROADCAST, worldKey, guildKey), loginNotification, charId);
		}
	}

	public void setChatroom(int chatroomId) {
		Byte worldKey = EventAddresses.worldKey(world);
		chatroom = chatroomId;
		if (chatroom != 0) {
			Integer chatroomKey = EventAddresses.chatroomKey(chatroom);
			ses.subscribe(String.format(EventAddresses.CHATROOM_BROADCAST, worldKey, chatroomKey));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.CHATROOM_BROADCAST, worldKey, chatroomKey), loginNotification, charId);
		}
	}
}
