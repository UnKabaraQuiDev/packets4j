package lu.pcy113.p4j.events.packets.s2c;

import lu.pcy113.p4j.events.packets.PacketEvent;
import lu.pcy113.p4j.packets.s2c.S2CPacket;

public interface S2CPacketEvent extends PacketEvent {

	S2CPacket getPacket();
	
}
