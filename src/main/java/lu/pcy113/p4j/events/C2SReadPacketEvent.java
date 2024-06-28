package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.server.ServerClient;

/**
 * Received on the server side, when a client sends a packet
 */
public class C2SReadPacketEvent implements P4JServerEvent {

	private ServerClient serverClient;
	private Packet packet;
	private Class<Packet> packetClass;

	private int id;
	private Throwable exception;
	private boolean fail;

	public C2SReadPacketEvent(ServerClient serverClient, Packet packet, Class<Packet> class1) {
		this.serverClient = serverClient;
		this.packet = packet;
		this.packetClass = class1;
	}

	public C2SReadPacketEvent(ServerClient serverClient, int id, Throwable e) {
		this.serverClient = serverClient;
		this.id = id;
		this.exception = e;
		this.fail = true;
	}

	public ServerClient getServerClient() {
		return serverClient;
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
