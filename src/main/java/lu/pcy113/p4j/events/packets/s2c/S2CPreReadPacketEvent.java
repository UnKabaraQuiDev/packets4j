package lu.pcy113.p4j.events.packets.s2c;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.events.P4JClientEvent;
import lu.pcy113.p4j.events.packets.pre.PreReadPacketEvent;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;

public class S2CPreReadPacketEvent implements PreReadPacketEvent, S2CReadPacketEvent, P4JClientEvent {

	private P4JClient client;

	private S2CPacket packet;

	private ByteBuffer bb;

	public S2CPreReadPacketEvent(P4JClient client, S2CPacket packet, ByteBuffer bb) {
		this.client = client;
		this.packet = packet;
		this.bb = bb;
	}

	@Override
	public ByteBuffer getBuffer() {
		return bb;
	}

	@Override
	public S2CPacket getPacket() {
		return packet;
	}

	@Override
	public P4JClient getClient() {
		return client;
	}

}
