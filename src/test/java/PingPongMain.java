import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Test;

import lu.pcy113.jbcodec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.P4JServer;
import lu.pcy113.pclib.logger.GlobalLogger;

public class PingPongMain {

	@Test
	public void test() {
		try {
			main(null);
		} catch (Exception e) {
			e.printStackTrace();
			assert false;
		}
	}

	public static void main(String args[]) throws Exception {

		GlobalLogger.init(null);

		P4JServer server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		server.bind(new InetSocketAddress(8361));
		server.getPackets().register(PingPongPacket.class, 1);
		server.setAccepting();

		GlobalLogger.info("server done");

		P4JClient client = new P4JClient(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		client.bind();
		client.getPackets().register(PingPongPacket.class, 1);
		client.connect(InetAddress.getLocalHost(), server.getLocalInetSocketAddress().getPort());
		GlobalLogger.info(client.getLocalInetSocketAddress());

		GlobalLogger.info("client done");

		GlobalLogger.info(client.write(new PingPongPacket()));

		client.close();
		server.close();

	}

}
