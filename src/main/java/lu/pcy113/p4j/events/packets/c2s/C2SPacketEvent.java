package lu.pcy113.p4j.events.packets.c2s;

import lu.pcy113.p4j.events.packets.PacketEvent;
import lu.pcy113.p4j.packets.c2s.C2SPacket;

public interface C2SPacketEvent extends PacketEvent {

	C2SPacket getPacket();

}
