package catdog;

import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.S2CReadPacketEvent;
import lu.pcy113.p4j.events.S2CWritePacketEvent;
import lu.pcy113.pclib.listener.EventDispatcher;
import lu.pcy113.pclib.listener.EventHandler;
import lu.pcy113.pclib.listener.EventListener;
import lu.pcy113.pclib.listener.EventManager;
import lu.pcy113.pclib.logger.GlobalLogger;

public class ClientEventListener implements EventListener {

	@EventHandler
	public void onClientConnect(ClientConnectedEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Client ClientConnectedEvent: " + ((ClientConnectedEvent) event).getClient() + " from: " + dispatcher);
	}

	@EventHandler
	public void onClientWrite(S2CWritePacketEvent event, EventManager em, EventDispatcher dispatcher) {
		if (((S2CWritePacketEvent) event).hasFailed()) {
			GlobalLogger.info("Client WritePacketEvent failed: " + ((S2CWritePacketEvent) event).getException() + " from: " + dispatcher);
		} else {
			GlobalLogger.info("Client WritePacketEvent: " + ((S2CWritePacketEvent) event).getPacket() + " from: " + dispatcher);
		}
	}

	@EventHandler
	public void onClientRead(S2CReadPacketEvent event, EventManager em, EventDispatcher dispatcher) {
		if (((S2CReadPacketEvent) event).hasFailed()) {
			GlobalLogger.info("Client ReadPacketEvent failed: " + ((S2CReadPacketEvent) event).getException() + " from: " + dispatcher);
		} else {
			GlobalLogger.info("Client ReadPacketEvent: " + ((S2CReadPacketEvent) event).getPacket() + " from: " + dispatcher);
		}
	}

}