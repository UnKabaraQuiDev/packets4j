package lu.pcy113.p4j.events.packets.c2s;

import lu.pcy113.p4j.events.P4JClientEvent;
import lu.pcy113.p4j.events.packets.FailPacketEvent;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.socket.client.P4JClient;

public class C2SWriteFailedPacketEvent implements FailPacketEvent, C2SWritePacketEvent, P4JClientEvent {

	private P4JClient client;

	private C2SPacket packet;

	private Throwable exception;

	public C2SWriteFailedPacketEvent(P4JClient client, C2SPacket packet) {
		this.client = client;
		this.packet = packet;
	}

	public C2SWriteFailedPacketEvent(P4JClient client, C2SPacket packet, Throwable exception) {
		this.client = client;
		this.packet = packet;
		this.exception = exception;
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
	public P4JClient getClient() {
		return client;
	}

}
