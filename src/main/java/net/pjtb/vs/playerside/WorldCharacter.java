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
		ses.unsubscribe(String.format(EventAddresses.MAP_BROADCAST, world, channel, map));
		map = mapId;
		ses.subscribe(String.format(EventAddresses.MAP_BROADCAST, world, channel, map));
		byte[] loginNotification = new byte[0];
		ses.publish(String.format(EventAddresses.MAP_BROADCAST, world, channel, map), loginNotification, charId);
	}

	public void setParty(int partyId) {
		if (party != 0)
			ses.unsubscribe(String.format(EventAddresses.PARTY_BROADCAST, world, channel, party));
		party = partyId;
		if (party != 0) {
			ses.subscribe(String.format(EventAddresses.PARTY_BROADCAST, world, channel, party));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.PARTY_BROADCAST, world, channel, party), loginNotification, charId);
		}
	}

	public void setGuild(int guildId) {
		guild = guildId;
		if (guild != 0) {
			ses.subscribe(String.format(EventAddresses.GUILD_BROADCAST, world, channel, guild));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.GUILD_BROADCAST, world, channel, guild), loginNotification, charId);
		}
	}

	public void setChatroom(int chatroomId) {
		chatroom = chatroomId;
		if (chatroom != 0) {
			ses.subscribe(String.format(EventAddresses.CHATROOM_BROADCAST, world, channel, chatroom));
			byte[] loginNotification = new byte[0];
			ses.publish(String.format(EventAddresses.CHATROOM_BROADCAST, world, channel, chatroom), loginNotification, charId);
		}
	}
}
