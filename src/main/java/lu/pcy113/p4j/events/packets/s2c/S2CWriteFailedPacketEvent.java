package lu.pcy113.p4j.events.packets.s2c;

import lu.pcy113.p4j.events.P4JServerEvent;
import lu.pcy113.p4j.events.packets.FailPacketEvent;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.server.ServerClient;

public class S2CWriteFailedPacketEvent implements FailPacketEvent, S2CWritePacketEvent, P4JServerEvent {

	private ServerClient client;

	private S2CPacket packet;

	private Throwable exception;

	public S2CWriteFailedPacketEvent(ServerClient client, S2CPacket packet) {
		this.client = client;
		this.packet = packet;
	}

	public S2CWriteFailedPacketEvent(ServerClient serverClient, S2CPacket packet, Throwable exception) {
		this.client = serverClient;
		this.packet = packet;
		this.exception = exception;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public S2CPacket getPacket() {
		return packet;
	}

	@Override
	public ServerClient getServerClient() {
		return client;
	}

}
