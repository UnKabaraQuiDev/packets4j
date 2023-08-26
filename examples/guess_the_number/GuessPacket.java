package guess_the_number;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;

public class GuessPacket implements C2SPacket<Integer> {

	private int guess;
	
	public GuessPacket() {}
	public GuessPacket(int gu) {
		this.guess = gu;
	}
	
	@Override
	public Integer clientWrite(P4JClient client) {
		return guess;
	}

	@Override
	public void serverRead(ServerClient sclient, Integer obj) {
		GuessTheNumberServer.games.get(sclient.getUUID()).gameGuess(obj);
	}

}
