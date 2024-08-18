package lu.pcy113.p4j.events.packets.s2c;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.events.P4JServerEvent;
import lu.pcy113.p4j.events.packets.pre.PreWritePacketEvent;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.server.ServerClient;

public class S2CPreWritePacketEvent implements PreWritePacketEvent, S2CWritePacketEvent, P4JServerEvent {

	private ServerClient serverClient;

	private S2CPacket packet;

	private ByteBuffer bb;

	public S2CPreWritePacketEvent(ServerClient serverClient, S2CPacket packet, ByteBuffer bb) {
		this.serverClient = serverClient;
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
	public ServerClient getServerClient() {
		return serverClient;
	}

}
