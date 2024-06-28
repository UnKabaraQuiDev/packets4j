package catdog;

import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.S2CReadPacketEvent;
import lu.pcy113.p4j.events.S2CWritePacketEvent;
import lu.pcy113.pclib.listener.EventHandler;
import lu.pcy113.pclib.listener.EventListener;
import lu.pcy113.pclib.logger.GlobalLogger;

public class ClientEventListener implements EventListener {
	
	@EventHandler
	public void onClientConnect(ClientConnectedEvent event) {
		GlobalLogger.info("Client ClientConnectedEvent: " + ((ClientConnectedEvent) event).getClient());
	}

	@EventHandler
	public void onClientWrite(S2CWritePacketEvent event) {
		if (((S2CWritePacketEvent) event).hasFailed())
			GlobalLogger.info("Client WritePacketEvent failed: " + ((S2CWritePacketEvent) event).getException());
		else
			GlobalLogger.info("Client WritePacketEvent: " + ((S2CWritePacketEvent) event).getPacket());
	}

	@EventHandler
	public void onClientRead(S2CReadPacketEvent event) {
		if (((S2CReadPacketEvent) event).hasFailed())
			GlobalLogger.info("Client ReadPacketEvent failed: " + ((S2CReadPacketEvent) event).getException());
		else
			GlobalLogger.info("Client ReadPacketEvent: " + ((S2CReadPacketEvent) event).getPacket());
	}
	
}