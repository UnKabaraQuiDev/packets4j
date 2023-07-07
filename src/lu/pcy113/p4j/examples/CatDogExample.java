package lu.pcy113.p4j.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Random;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.P4JServer;
import lu.pcy113.p4j.socket.server.ServerClient;

public class CatDogExample {

	private static P4JServer server;
	private static P4JClient client;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// CLEATE A SERVER
		
		CodecManager serverCodec = CodecManager.base();
		EncryptionManager serverEncryption = EncryptionManager.raw();
		CompressionManager serverCompression = CompressionManager.raw();
		server = new P4JServer(serverCodec, serverEncryption, serverCompression) {
			@Override
			public void clientConnected(ServerClient client) {
				sendChoiceRequest(client); // See "Send Packets"
			}
		};

		// Register incoming and outdoing packets
		// Because S2C packet takes a String[] and C2S packet takes a String
		// We can't use the same id, because the classes haven't the same argument
		// registerPacket() is a shortcut for getPackets().register()
		server.getPackets().register(C2S_CatDogPacket.class, 1);
		server.registerPacket(S2C_CatDogPacket.class, 2);

		// Bind to the local port
		server.bind(new InetSocketAddress(8090));
		System.out.println("Server bound");
		
		// Set as listening and accepting clients
		server.setAccepting();
		System.out.println("Server listening and accepting clients");
		
		
		// CREATE A CLIENT
		
		CodecManager clientCodec = CodecManager.base();
		EncryptionManager clientEncryption = EncryptionManager.raw();
		CompressionManager clientCompression = CompressionManager.raw();
		client = new P4JClient(clientCodec, clientEncryption, clientCompression);

		// Same as the Server
		client.getPackets().register(C2S_CatDogPacket.class, 1);
		client.registerPacket(S2C_CatDogPacket.class, 2);

		// Bind without any argument takes a free port, a specific port can be passed as argument
		client.bind();
		System.out.println("Client bound");
		
		
		// CONNECT THE CLIENT TO THE SERVER
		
		// Connect to the server
		client.connect(server.getLocalInetSocketAddress());
		System.out.println("Client connected");
		
		Thread.sleep(5000);
		
		
		client.close();
		
		server.close();
		
	}
	
	// See "Create a Server"
	private static void sendChoiceRequest(ServerClient client) {
		System.out.println("Client connected to server");
		
		// Send a packet to the newly connected client
		System.out.println(client.write(new S2C_CatDogPacket()));
		
		// OR
		
		// Broadcast a packet to all clients
		//server.broadcast(new S2C_CatDogPacket());
	}
	
	
	// CREATE A PACKET
	
	// This class is used to manage the communication between the Server → Client
	// It describes how to handle the received String[] and what value to send
	public static class S2C_CatDogPacket implements S2CPacket<ArrayList<String>> {
		
		// Gets called when a Client receives this packet from the connected server
		public void clientRead(P4JClient client, ArrayList<String> input) {
			System.out.println("Question received: ");
			System.out.println(input);
			Random r = new Random();
			int choiceIndex = r.nextInt(input.size());
			client.write(new C2S_CatDogPacket(input.get(choiceIndex)));
		}

		// Gets called when using ServerClient.write(new S2C_CatDogPacket())
		// Returns the value to be sent
		public ArrayList<String> serverWrite(ServerClient client) {
			System.out.println("Asked to client");
			ArrayList<String> al = new ArrayList<>();
			al.add("Dog");
			al.add("or");
			al.add("Cat");
			return al;
		}
	}

	// This class is used to manage the communication between the Client → Server
	// It describes how to handle the received String and what value to send
	public static class C2S_CatDogPacket implements C2SPacket<String> {
		String choice;

		public C2S_CatDogPacket(String choice) {
			System.out.println("Choice prepared: "+choice);
			this.choice = choice;
		}

		// Gets called when using P4JClient.write(new C2S_CatDogPacket())
		// Returns the value to be sent
		public String clientWrite(P4JClient client) {
			System.out.println("Responding to server: "+choice);
			return this.choice;
		}

		// Gets called when a Server receives this packet from a connected Client
	    public void serverRead(ServerClient sclient, String obj) {
			System.out.println("Client answered: "+obj);
		}
	}

}
