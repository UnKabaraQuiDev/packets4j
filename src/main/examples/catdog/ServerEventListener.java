package catdog;

import lu.pcy113.p4j.events.client.ClientConnectedEvent;
import lu.pcy113.p4j.events.packets.c2s.C2SReadFailedPacketEvent;
import lu.pcy113.p4j.events.packets.c2s.C2SWriteFailedPacketEvent;
import lu.pcy113.p4j.events.packets.post.PostReadPacketEvent;
import lu.pcy113.p4j.events.packets.post.PostWritePacketEvent;
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
	public void onServerWriteFailed(C2SWriteFailedPacketEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Client WritePacketEvent failed: " + event.getException() + " from: " + dispatcher);
	}

	@EventHandler
	public void onServerWriteSuccess(PostWritePacketEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Client PostWritePacketEvent: " + event.getPacket() + " from: " + dispatcher);
	}

	@EventHandler
	public void onServerReadFailed(C2SReadFailedPacketEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Client ReadPacketEvent failed: " + event.getException() + " from: " + dispatcher);
	}

	@EventHandler
	public void onServerReadSuccess(PostReadPacketEvent event, EventManager em, EventDispatcher dispatcher) {
		GlobalLogger.info("Client PostReadPacketEvent: " + event.getPacket() + " from: " + dispatcher);
	}

}