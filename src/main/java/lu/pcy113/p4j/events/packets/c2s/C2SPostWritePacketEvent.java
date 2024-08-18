package lu.pcy113.p4j.events.packets.c2s;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.events.P4JClientEvent;
import lu.pcy113.p4j.events.packets.post.PostWritePacketEvent;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.socket.client.P4JClient;

public class C2SPostWritePacketEvent implements PostWritePacketEvent, C2SWritePacketEvent, P4JClientEvent {

	private P4JClient client;

	private C2SPacket packet;
	
	private ByteBuffer bb;

	public C2SPostWritePacketEvent(P4JClient client, C2SPacket packet, ByteBuffer bb) {
		this.client = client;
		this.packet = packet;
		this.bb = bb;
	}
	
	@Override
	public ByteBuffer getBuffer() {
		return bb;
	}

	@Override
	public C2SPacket getPacket() {
		return packet;
	}

	@Override
	public P4JClient getClient() {
		return client;
	}

}
