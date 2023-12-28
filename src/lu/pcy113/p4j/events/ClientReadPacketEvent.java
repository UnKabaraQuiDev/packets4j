package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.P4JClientInstance;

public class ClientReadPacketEvent implements Event {

	private P4JClientInstance client;
	private Packet packet;
	private Class<Packet> packetClass;

	private int id;
	private Throwable exception;
	private boolean fail;

	public ClientReadPacketEvent(P4JClientInstance p4jClient, Packet packet, Class<Packet> class1) {
		this.client = p4jClient;
		this.packet = packet;
		this.packetClass = class1;
	}

	public ClientReadPacketEvent(P4JClientInstance p4jClient, int id, Throwable e) {
		this.client = p4jClient;
		this.id = id;
		this.exception = e;
		this.fail = true;
	}

	public P4JClientInstance getClient() {return client;}
	public Packet getPacket() {return packet;}
	public Class<Packet> getPacketClass() {return packetClass;}
	public int getPacketId() {return id;}
	public Throwable getException() {return exception;}
	public boolean hasFailed() {return fail;}

}
