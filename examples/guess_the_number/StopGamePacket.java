package guess_the_number;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;

/**
 * @author pcy113
 *
 * The String represents the reason for disconnection
 *
 */
public class StopGamePacket implements C2SPacket<String>, S2CPacket<String> {

	private String reason;
	
	public StopGamePacket() {}
	public StopGamePacket(String r) {
		this.reason = r;
	}
	
	@Override
	public String clientWrite(P4JClient client) {
		return reason;
	}

	@Override
	public void serverRead(ServerClient sclient, String obj) {
		GuessTheNumberServer.games.get(sclient.getUUID()).requestDisconnection(obj);
	}

	@Override
	public String serverWrite(ServerClient client) {
		return reason;
	}

	@Override
	public void clientRead(P4JClient client, String obj) {
		
	}

}
