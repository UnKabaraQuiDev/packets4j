package guess_the_number;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;

public class StartGamePacket implements S2CPacket<Range>, C2SPacket<Object> {

	private Range range;
	
	public StartGamePacket() {}
	public StartGamePacket(Range r) {
		this.range = r;
	}
	
	@Override
	public Range serverWrite(ServerClient client) {
		return range;
	}

	@Override
	public void clientRead(P4JClient client, Range obj) {
		((GuessTheNumberClient) client).gameStart(obj);
	}
	@Override
	public Object clientWrite(P4JClient client) {
		return null;
	}
	@Override
	public void serverRead(ServerClient sclient, Object obj) {
		GuessTheNumberServer.games.get(sclient.getUUID()).gameStart();
	}

}
