import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;

import lu.pcy113.jbcodec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.ClosedSocketEvent;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.P4JServer;
import lu.pcy113.pclib.listener.EventHandler;
import lu.pcy113.pclib.listener.EventListener;
import lu.pcy113.pclib.logger.GlobalLogger;

public class EventMain {

	public class ServerEventListener implements EventListener {

		private String target;

		public ServerEventListener(String trg) {
			this.target = trg.toUpperCase();
		}

		@EventHandler
		public void onConnect(ClientConnectedEvent evt) {
			GlobalLogger.log(target + " client connected: " + evt.getClient());
		}

		@EventHandler
		public void onClosed(ClosedSocketEvent evt) {
			GlobalLogger.log(target + " socket closed: " + evt.getClient());
		}
	}

	@Test
	public void events() {
		try {
			if (!GlobalLogger.isInit()) {
				GlobalLogger.init(null);
			}

			P4JServer server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
			server.bind(new InetSocketAddress(8361));
			server.getPackets().register(PingPongPacket.class, 1);
			server.getEventManager().register(new ServerEventListener("server"));
			server.setAccepting();

			GlobalLogger.info("server done");

			P4JClient client = new P4JClient(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
			client.bind();
			client.getPackets().register(PingPongPacket.class, 1);
			client.getEventManager().register(new ServerEventListener("client"));
			client.connect(InetAddress.getLocalHost(), server.getLocalInetSocketAddress().getPort());
			GlobalLogger.info("client addr: " + client.getLocalInetSocketAddress());
			GlobalLogger.info("client remote: " + client.getClientServer().getRemoteInetSocketAddress());

			GlobalLogger.info("client done");

			GlobalLogger.info("client sent packet: " + client.write(new PingPongPacket()));

			Thread.sleep(2000); // simulate traffic
			
			client.disconnect();
			GlobalLogger.info("client closed");

			client.join();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
			assert false;
		}
	}

}
