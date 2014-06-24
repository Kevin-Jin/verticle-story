package net.pjtb.vs.shared;

import net.pjtb.vs.mapside.ChannelMap;
import net.pjtb.vs.playerside.Chatroom;
import net.pjtb.vs.playerside.Guild;
import net.pjtb.vs.playerside.Party;

public final class EventAddresses {
	public static final String MAP_REQUEST = ChannelMap.class.getCanonicalName() + "[%d,%d].request";
	public static final String MAP_BROADCAST = ChannelMap.class.getCanonicalName() + "[%d,%d][%09d].broadcast";
	public static final String PARTY_BROADCAST = Party.class.getCanonicalName() + "[%d,%d][%010d].broadcast";
	public static final String PARTY_REQUEST = Party.class.getCanonicalName() + "[%d].request";
	public static final String GUILD_BROADCAST = Guild.class.getCanonicalName() + "[%d,%d][%010d].broadcast";
	public static final String CHATROOM_BROADCAST = Chatroom.class.getCanonicalName() + "[%d,%d][%010d].broadcast";

	private EventAddresses() {
		
	}
}
