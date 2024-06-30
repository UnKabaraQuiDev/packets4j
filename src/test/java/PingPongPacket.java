import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;
import lu.pcy113.pclib.logger.GlobalLogger;

public class PingPongPacket implements C2SPacket<Long>, S2CPacket<Long> {

	private long current;

	public PingPongPacket() {
	}

	public PingPongPacket(long l) {
		current = l;
	}

	@Override
	public Long clientWrite(P4JClient client) {
		long x = System.currentTimeMillis();
		return x;
	}

	@Override
	public void serverRead(ServerClient sclient, Long obj) {
		GlobalLogger.info("server read");
		GlobalLogger.info("server packet sent: "+sclient.write(new PingPongPacket(obj)));
	}

	@Override
	public void clientRead(P4JClient client, Long obj) {
		GlobalLogger.info("client read");
	}

	@Override
	public Long serverWrite(ServerClient client) {
		long x = System.currentTimeMillis() - current;
		return x;
	}

}
