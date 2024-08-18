package catdog;

import lu.pcy113.p4j.events.client.ClientConnectedEvent;
import lu.pcy113.p4j.events.packets.s2c.S2CReadPacketEvent;
import lu.pcy113.p4j.events.packets.s2c.S2CWritePacketEvent;
import lu.pcy113.p4j.socket.server.ServerClient;
import lu.pcy113.pclib.listener.EventDispatcher;
import lu.pcy113.pclib.listener.EventHandler;
import lu.pcy113.pclib.listener.EventListener;
import lu.pcy113.pclib.listener.EventManager;
import lu.pcy113.pclib.logger.GlobalLogger;

public class ServerEventListener implements EventListener {

	@EventHandler
	public void onClientConnect(ClientConnectedEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Server ClientConnectedEvent: " + ((ClientConnectedEvent) event).getClient() + " from: " + dispatcher);
		CatDogExample.sendChoiceRequest((ServerClient) ((ClientConnectedEvent) event).getClient()); // See "Send Packets"
	}

	@EventHandler
	public void onClientWrite(S2CWritePacketEvent event, EventManager em, EventDispatcher dispatcher) {
		if (event.hasFailed()) {
			GlobalLogger.info("Server ClientWritePacketEvent failed: " + event.getException() + " from: " + dispatcher);
		} else {
			GlobalLogger.info("Server ClientWritePacketEvent: " + event.getPacket() + " from: " + dispatcher);
		}
	}

	@EventHandler
	public void onClientRead(S2CReadPacketEvent event, EventManager em, EventDispatcher dispatcher) {
		if (event.hasFailed()) {
			GlobalLogger.info("Server ClientReadPacketEvent failed: " + event.getException() + " from: " + dispatcher);
		} else {
			GlobalLogger.info("Server ClientReadPacketEvent: " + event.getPacket() + " from: " + dispatcher);
		}
	}

}