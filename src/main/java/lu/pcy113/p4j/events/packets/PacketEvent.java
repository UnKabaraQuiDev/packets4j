package lu.pcy113.p4j.events.packets;

import lu.pcy113.p4j.events.P4JEvent;
import lu.pcy113.p4j.packets.Packet;

public interface PacketEvent extends P4JEvent {

	Packet getPacket();

}
