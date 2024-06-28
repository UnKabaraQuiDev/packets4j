package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.client.P4JClient;

/**
 * Received on the client side, when a client sends a packet
 */
public class C2SWritePacketEvent implements P4JClientEvent {

	private P4JClient client;
	private Packet packet;

	private Throwable exception;
	private boolean fail;

	public C2SWritePacketEvent(P4JClient serverClient, Packet packet) {
		this.client = serverClient;
		this.packet = packet;
	}

	public C2SWritePacketEvent(P4JClient serverClient, Packet packet, Throwable e) {
		this.client = serverClient;
		this.packet = packet;
		this.exception = e;
		this.fail = true;
	}

	public P4JClient getClient() {
		return client;
	}

	public Packet getPacket() {
		return packet;
	}

	public Throwable getException() {
		return exception;
	}

	public boolean hasFailed() {
		return fail;
	}

}
