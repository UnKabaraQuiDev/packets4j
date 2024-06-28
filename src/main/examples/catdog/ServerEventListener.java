package catdog;

import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.S2CReadPacketEvent;
import lu.pcy113.p4j.events.S2CWritePacketEvent;
import lu.pcy113.p4j.socket.server.ServerClient;
import lu.pcy113.pclib.listener.EventHandler;
import lu.pcy113.pclib.listener.EventListener;
import lu.pcy113.pclib.logger.GlobalLogger;

public class ServerEventListener implements EventListener {
	
	@EventHandler
	public void onClientConnect(ClientConnectedEvent event) {
		GlobalLogger.info("Server ClientConnectedEvent: " + ((ClientConnectedEvent) event).getClient());
		CatDogExample.sendChoiceRequest((ServerClient) ((ClientConnectedEvent) event).getClient()); // See "Send Packets"
	}

	@EventHandler
	public void onClientWrite(S2CWritePacketEvent event) {
		if (((S2CWritePacketEvent) event).hasFailed())
			GlobalLogger.info("Server ClientWritePacketEvent failed: " + ((S2CWritePacketEvent) event).getException());
		else
			GlobalLogger.info("Server ClientWritePacketEvent: " + ((S2CWritePacketEvent) event).getPacket());
	}

	@EventHandler
	public void onClientRead(S2CReadPacketEvent event) {
		if (((S2CReadPacketEvent) event).hasFailed())
			GlobalLogger.info("Server ClientReadPacketEvent failed: " + ((S2CReadPacketEvent) event).getException());
		else
			GlobalLogger.info("Server ClientReadPacketEvent: " + ((S2CReadPacketEvent) event).getPacket());
	}
	
}