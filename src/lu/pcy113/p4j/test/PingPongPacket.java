package lu.pcy113.p4j.test;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;

public class PingPongPacket implements C2SPacket<Long>, S2CPacket<Long> {

	public PingPongPacket() {}
	
	@Override
	public Long clientWrite(P4JClient client) {
		long x = System.currentTimeMillis();
		return x;
	}
	@Override
	public void serverRead(ServerClient sclient, Long obj) {
		System.out.println("server read");
		sclient.write(new PingPongPacket(obj));
	}
	
	
	private long current;
	public PingPongPacket(long l) {
		current = l;
	}
	
	@Override
	public void clientRead(P4JClient client, Long obj) {
		System.out.println("client read");
	}
	
	@Override
	public Long serverWrite(ServerClient client) {
		long x = System.currentTimeMillis() - current;
		return x;
	}
	
}
