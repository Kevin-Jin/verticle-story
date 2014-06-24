package net.pjtb.vs.playerside;

import net.pjtb.vs.util.LittleEndianReader;

public interface GameSidePacketProcessor {
	public void process(LittleEndianReader reader);
}
