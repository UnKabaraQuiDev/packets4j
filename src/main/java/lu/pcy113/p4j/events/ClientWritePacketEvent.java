package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.socket.P4JClientInstance;

public class ClientWritePacketEvent implements Event {

	private P4JClientInstance serverClient;
	private Packet packet;

	private Throwable exception;
	private boolean fail;

	public ClientWritePacketEvent(P4JClientInstance serverClient, Packet packet) {
		this.serverClient = serverClient;
		this.packet = packet;
	}

	public ClientWritePacketEvent(P4JClientInstance serverClient2, Packet packet2, Throwable e) {
		this.serverClient = serverClient2;
		this.packet = packet2;
		this.exception = e;
		this.fail = true;
	}

	public P4JClientInstance getServerClient() {return serverClient;}
	public Packet getPacket() {return packet;}
	public Throwable getException() {return exception;}
	public boolean hasFailed() {return fail;}

}
