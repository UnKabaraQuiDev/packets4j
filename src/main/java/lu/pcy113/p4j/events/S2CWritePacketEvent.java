package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.server.ServerClient;

/**
 * Received on the server side, when a server sends a packet
 */
public class S2CWritePacketEvent implements P4JServerEvent {

	private ServerClient serverClient;
	private Packet packet;
	private int packetId;

	private Throwable exception;

	public S2CWritePacketEvent(ServerClient serverClient, Packet packet, int id) {
		this.serverClient = serverClient;
		this.packet = packet;
		this.packetId = id;
	}

	public S2CWritePacketEvent(ServerClient serverClient, Packet packet, Throwable e) {
		this.serverClient = serverClient;
		this.packet = packet;
		this.exception = e;
	}

	public int getPacketId() {
		return packetId;
	}

	public ServerClient getServerClient() {
		return serverClient;
	}

	public Packet getPacket() {
		return packet;
	}

	public Throwable getException() {
		return exception;
	}

	public boolean hasFailed() {
		return exception != null;
	}

}
