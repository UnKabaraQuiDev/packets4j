package lu.pcy113.p4j.events.packets;

import java.nio.ByteBuffer;

public interface ReadPacketEvent extends PacketEvent {

	ByteBuffer getBuffer();

}
