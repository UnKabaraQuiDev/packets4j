package guess_the_number;

import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.ServerClient;

public class FeedbackPacket implements S2CPacket<String> {

	private String message;
	
	public FeedbackPacket() {}
	public FeedbackPacket(String msg) {
		this.message = msg;
	}
	
	@Override
	public String serverWrite(ServerClient client) {
		return message;
	}

	@Override
	public void clientRead(P4JClient client, String obj) {
		((GuessTheNumberClient) client).gameFeedback(message);
	}

}
