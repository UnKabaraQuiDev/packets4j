package lu.pcy113.p4j.events.packets.s2c;

import lu.pcy113.p4j.events.P4JClientEvent;
import lu.pcy113.p4j.events.packets.FailPacketEvent;
import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;

public class S2CReadFailedPacketEvent implements FailPacketEvent, S2CReadPacketEvent, P4JClientEvent {

	private P4JClient client;

	private S2CPacket packet;
	private Class<Packet> packetClass;
	private int packetId;

	private Throwable exception;

	public S2CReadFailedPacketEvent(P4JClient client, S2CPacket packet, Throwable exception) {
		this.client = client;
		this.packet = packet;
		this.exception = exception;
	}

	public S2CReadFailedPacketEvent(P4JClient client, Class<Packet> packetClass, Throwable exception) {
		this.client = client;
		this.packetClass = packetClass;
		this.exception = exception;
	}

	public S2CReadFailedPacketEvent(P4JClient client, int packetId, Throwable exception) {
		this.client = client;
		this.packetId = packetId;
		this.exception = exception;
	}

	public int getPacketId() {
		return packetId;
	}

	public Class<Packet> getPacketClass() {
		return packetClass;
	}

	@Override
	public S2CPacket getPacket() {
		return packet;
	}

	@Override
	public P4JClient getClient() {
		return client;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

}
