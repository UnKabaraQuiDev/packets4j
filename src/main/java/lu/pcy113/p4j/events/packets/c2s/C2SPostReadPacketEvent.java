package lu.pcy113.p4j.events.packets.c2s;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.events.P4JServerEvent;
import lu.pcy113.p4j.events.packets.post.PostReadPacketEvent;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.socket.server.ServerClient;

public class C2SPostReadPacketEvent implements PostReadPacketEvent, C2SReadPacketEvent, P4JServerEvent {

	private ServerClient server;

	private C2SPacket packet;

	private ByteBuffer bb;

	public C2SPostReadPacketEvent(ServerClient server, C2SPacket packet, ByteBuffer bb) {
		this.server = server;
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
	public ServerClient getServerClient() {
		return server;
	}

}
