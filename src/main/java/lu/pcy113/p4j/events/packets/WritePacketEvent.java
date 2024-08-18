package lu.pcy113.p4j.events.packets;

import java.nio.ByteBuffer;

public interface WritePacketEvent extends PacketEvent {

	ByteBuffer getBuffer();
	
}
