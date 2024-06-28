package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.client.P4JClient;

/**
 * Received on the client side, when a server sends a packet
 */
public class S2CReadPacketEvent implements P4JClientEvent {

	private P4JClient client;
	private Packet packet;
	private Class<Packet> packetClass;

	private int id;
	private Throwable exception;
	private boolean fail;

	public S2CReadPacketEvent(P4JClient client, Packet packet, Class<Packet> class1) {
		this.client = client;
		this.packet = packet;
		this.packetClass = class1;
	}

	public S2CReadPacketEvent(P4JClient client, int id, Throwable e) {
		this.client = client;
		this.id = id;
		this.exception = e;
		this.fail = true;
	}

	public P4JClient getClient() {
		return client;
	}

	public Packet getPacket() {
		return packet;
	}

	public Class<Packet> getPacketClass() {
		return packetClass;
	}

	public int getPacketId() {
		return id;
	}

	public Throwable getException() {
		return exception;
	}

	public boolean hasFailed() {
		return fail;
	}

}
