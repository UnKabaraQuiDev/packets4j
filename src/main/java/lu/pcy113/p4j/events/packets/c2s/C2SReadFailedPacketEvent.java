package lu.pcy113.p4j.events.packets.c2s;

import lu.pcy113.p4j.events.P4JServerEvent;
import lu.pcy113.p4j.events.packets.FailPacketEvent;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.socket.server.ServerClient;

public class C2SReadFailedPacketEvent implements FailPacketEvent, C2SReadPacketEvent, P4JServerEvent {

	private ServerClient server;

	private C2SPacket packet;
	private int packetId;

	private Throwable exception;

	public C2SReadFailedPacketEvent(ServerClient server, C2SPacket packet, Throwable exception) {
		this.server = server;
		this.packet = packet;
		this.exception = exception;
	}

	public C2SReadFailedPacketEvent(ServerClient server, int packetId, Exception exception) {
		this.server = server;
		this.packetId = packetId;
		this.exception = exception;
	}

	public int getPacketId() {
		return packetId;
	}

	@Override
	public Throwable getException() {
		return exception;
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
