package net.pjtb.vs.shared;

import net.pjtb.vs.mapside.ChannelMap;
import net.pjtb.vs.playerside.Chatroom;
import net.pjtb.vs.playerside.Guild;
import net.pjtb.vs.playerside.Party;

public final class EventAddresses {
	/**
	 * Sample usage: String.format(EventAddresses.MAP_REQUEST, EventAddresses.fullyQualifiedChannelKey(world, channel));
	 */
	public static final String MAP_REQUEST = ChannelMap.class.getCanonicalName() + "[%05d].request";
	/**
	 * Sample usage: String.format(EventAddresses.MAP_BROADCAST, EventAddresses.fullyQualifiedChannelKey(world, channel), EventAddresses.mapKey(mapId));
	 */
	public static final String MAP_BROADCAST = ChannelMap.class.getCanonicalName() + "[%05d][%09d].broadcast";
	/**
	 * Sample usage: String.format(EventAddresses.PARTY_BROADCAST, EventAddresses.worldKey(world), EventAddresses.partyKey(partyId));
	 */
	public static final String PARTY_BROADCAST = Party.class.getCanonicalName() + "[%03d][%010d].broadcast";
	/**
	 * Sample usage: String.format(EventAddresses.PARTY_REQUEST, EventAddresses.worldKey(world));
	 */
	public static final String PARTY_REQUEST = Party.class.getCanonicalName() + "[%03d].request";
	/**
	 * Sample usage: String.format(EventAddresses.GUILD_BROADCAST, EventAddresses.worldKey(world), EventAddresses.guildKey(guildId));
	 */
	public static final String GUILD_BROADCAST = Guild.class.getCanonicalName() + "[%03d][%010d].broadcast";
	/**
	 * Sample usage: String.format(EventAddresses.CHATROOM_BROADCAST, EventAddresses.worldKey(world), EventAddresses.chatroomKey(chatroomd));
	 */
	public static final String CHATROOM_BROADCAST = Chatroom.class.getCanonicalName() + "[%03d][%010d].broadcast";
	/**
	 * Sample usage: String.format(EventAddresses.SPECS_LOADED_NOTIFICATION, "map");
	 */
	public static final String SPECS_LOADED_NOTIFICATION = Specs.class.getCanonicalName() + "[%s].done";

	public static final short LOGIN_CH = -2;
	public static final short OFFLINE_CH = -1;
	public static final short SHOP_CH = 20;

	public static Short fullyQualifiedChannelKey(byte world, byte channel) {
		return Short.valueOf((short) (world * 100 + channel));
	}

	public static Byte worldKey(byte world) {
		return Byte.valueOf(world);
	}

	public static Byte channelKey(byte channel) {
		return Byte.valueOf(channel);
	}

	public static Integer mapKey(int mapId) {
		return Integer.valueOf(mapId);
	}

	public static Integer partyKey(int partyId) {
		return Integer.valueOf(partyId);
	}

	public static Integer guildKey(int guildId) {
		return Integer.valueOf(guildId);
	}

	public static Integer chatroomKey(int chatroomId) {
		return Integer.valueOf(chatroomId);
	}

	private EventAddresses() {
		
	}
}
