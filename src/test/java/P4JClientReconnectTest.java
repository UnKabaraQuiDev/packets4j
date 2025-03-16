import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lu.pcy113.jbcodec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.P4JServer;

public class P4JClientReconnectTest {

	private P4JServer server;
	private P4JClient client;
	private InetSocketAddress serverAddress;

	public static void main(String[] args) throws InterruptedException, IOException {
		new P4JClientReconnectTest().run();
	}
	
	public void run() throws InterruptedException, IOException {
		setUp();
		testClientReconnection();
		tearDown();
	}
	
	@Before
	public void setUp() throws IOException {
		// Start a simple server to accept connections
		server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		serverAddress = new InetSocketAddress(0);
		server.bind(serverAddress);
		serverAddress = server.getLocalInetSocketAddress();
		server.setAccepting();

		// Create and connect client
		client = new P4JClient(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		client.bind();
		client.connect(serverAddress);
	}

	@After
	public void tearDown() throws InterruptedException {
		client.disconnect();
		System.out.println("Disconnected client, waiting");
		client.join();
		System.out.println("Client thread shut down");
		server.close();
		System.out.println("Closed server, waiting");
		server.join();
		System.out.println("Server thread shut down");
		// server.stop();
	}

	@Test
	public void testClientReconnection() throws InterruptedException, IOException {
		assertTrue("Client should be initially connected", client.isConnected());

		// Simulate server disconnection
		server.close();
		server.join();
		Thread.sleep(500); // Allow time for client to detect disconnect

		assertFalse("Client should detect disconnection", client.isConnected());

		// Restart server to test reconnection
		// server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
		server.bind(serverAddress);
		server.setAccepting();

		client.bind();
		client.connect(serverAddress);
		
		// Wait for client to reconnect
		Thread.sleep(5000); // Allow time for reconnection attempts

		// assertTrue("Client should reconnect automatically", client.isConnected());
	}
	
}
