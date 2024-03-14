# CatDog example

- - -

*This example is dumb, but it covers everything useful, except overriden ServerClients*

- - -

Create a Packet:
```java
// This class is used to manage the communication between the Server → Client
// It describes how to handle the received String[] and what value to send
public static class S2C_CatDogPacket implements S2CPacket<Object[]> {

	// Gets called when a Client receives this packet from the connected server
	public void clientRead(P4JClient client, Object[] input) {
		System.out.println("Question received: ");
		System.out.println(Arrays.toString(input));
		Random r = new Random();
		int choiceIndex = r.nextInt(input.length);

		// We can cast to a String because we're sure serverWrite() returns String[]
		client.write(new C2S_CatDogPacket((String) input[choiceIndex]));
	}

	// Gets called when using ServerClient.write(new S2C_CatDogPacket())
	// Returns the value to be sent
	public Object[] serverWrite(ServerClient client) {
		System.out.println("Asked to client");
		return new String[] { "Dog", "or", "Cat" };
	}
}

// This class is used to manage the communication between the Client → Server
// It describes how to handle the received String and what value to send
public static class C2S_CatDogPacket implements C2SPacket<String> {
	String choice;

	// A constructor with no argument is needed or a PacketInstanceException will be thrown
	public C2S_CatDogPacket() {
	}

	public C2S_CatDogPacket(String choice) {
		System.out.println("Choice prepared: " + choice);
		this.choice = choice;
	}

	// Gets called when using P4JClient.write(new C2S_CatDogPacket())
	// Returns the value to be sent
	public String clientWrite(P4JClient client) {
		System.out.println("Responding to server: " + choice);
		return this.choice;
	}

	// Gets called when a Server receives this packet from a connected Client
	public void serverRead(ServerClient sclient, String obj) {
		System.out.println("Client answered: " + obj);
	}
}
```
The `S2CPacket` and `C2SPacket` interfaces could be implementing the same Object.

Create a Server:
```java
CodecManager serverCodec = CodecManager.base();
serverCodec.register(new ArrayEncoder(), new ArrayDecoder(), (short) 11);
EncryptionManager serverEncryption = EncryptionManager.raw();
CompressionManager serverCompression = CompressionManager.raw();
server = new P4JServer(serverCodec, serverEncryption, serverCompression);
server.setEventQueueConsumer(new AsyncEventQueueConsumer());

// Attach a listener to handle new connected clients
server.events.addListener(new Listener() {
	@Override
	public void handle(Event event) { // conntected
		System.out.println("Server event: " + event.getClass().getSimpleName());
		if (event instanceof ClientConnectedEvent) {
			System.out.println("Server ClientConnectedEvent: "+((ClientConnectedEvent) event).getClient());
			sendChoiceRequest((ServerClient) ((ClientConnectedEvent) event).getClient()); // See "Send Packets"
		}
		
		if (event instanceof ClientWritePacketEvent)
			if(((ClientWritePacketEvent) event).hasFailed())
				System.out.println("Server ClientWritePacketEvent failed: " + ((ClientWritePacketEvent) event).getException());
			else
				System.out.println("Server ClientWritePacketEvent: " + ((ClientWritePacketEvent) event).getPacket());
		
		if (event instanceof ClientReadPacketEvent)
			if(((ClientReadPacketEvent) event).hasFailed())
				System.out.println("Server ClientReadPacketEvent failed: " + ((ClientReadPacketEvent) event).getException());
			else
				System.out.println("Server ClientReadPacketEvent: " + ((ClientReadPacketEvent) event).getPacket());
			
	}
});

// Register incoming and outdoing packets
// We can't use the same id, because the classes haven't the same argument
// S2C packet takes a Object[] and C2S packet takes a String
server.getPackets().register(C2S_CatDogPacket.class, 1);
// registerPacket() is a shortcut for getPackets().register()
server.registerPacket(S2C_CatDogPacket.class, 2);

// Bind to the local port
server.bind(new InetSocketAddress(8090));
System.out.println("Server bound to port: " + server.getPort());

// Set as listening and accepting clients
// Starts the server thread
server.setAccepting();
System.out.println("Server listening and accepting clients");
```

Create a Client:
```java
CodecManager clientCodec = CodecManager.base();
clientCodec.register(new ArrayEncoder(), new ArrayDecoder(), (short) 11);
EncryptionManager clientEncryption = EncryptionManager.raw();
CompressionManager clientCompression = CompressionManager.raw();
client = new P4JClient(clientCodec, clientEncryption, clientCompression);
client.setEventQueueConsumer(new AsyncEventQueueConsumer());

client.events.addListener(new Listener() {
	@Override
	public void handle(Event event) { // conntected
		System.out.println("Server event: " + event.getClass().getSimpleName());
		if (event instanceof ClientConnectedEvent)
			System.out.println("Client ClientConnectedEvent: "+((ClientConnectedEvent) event).getClient());
		
		if (event instanceof ClientWritePacketEvent)
			if(((ClientWritePacketEvent) event).hasFailed())
				System.out.println("Client WritePacketEvent failed: " + ((ClientWritePacketEvent) event).getException());
			else
				System.out.println("Client WritePacketEvent: " + ((ClientWritePacketEvent) event).getPacket());
		
		if (event instanceof ClientReadPacketEvent)
			if(((ClientReadPacketEvent) event).hasFailed())
				System.out.println("Client ReadPacketEvent failed: " + ((ClientReadPacketEvent) event).getException());
			else
				System.out.println("Client ReadPacketEvent: " + ((ClientReadPacketEvent) event).getPacket());
	}
});

// Same as the Server
client.getPackets().register(C2S_CatDogPacket.class, 1);
client.registerPacket(S2C_CatDogPacket.class, 2);

// Bind without any argument takes a free port, a specific port can be passed as argument
client.bind();
System.out.println("Client bound to port: " + client.getPort());
```

Connect the Client:
```java
// Connect to the server
client.connect(server.getLocalInetSocketAddress());
System.out.println("Client connected");
```

Send Packets:
```java
// See "Create a Server"
// This function gets called when a new client connects
private static void sendChoiceRequest(ServerClient client) {
	System.out.println("Client connected to server");

	// Send a packet to the newly connected client
	System.out.println("Packet sent to client: " + client.write(new  S2C_CatDogPacket()));

	// OR

	// Broadcast a packet to all clients
	// server.broadcast(new S2C_CatDogPacket());
}
```

In this example, the server-client packet exchange should look like this:
| ORDER | DIR | TYPE | OBJECT | VALUE | FUNCTION |
|------|-----|------|--------|-------|-----------|
| 1. | send | S2C | String[] | input → {"Cat", "or", "Dog"} | serverWrite(ServerClient) → String[] | 
| 2. | read | S2C | String[] | input						| clientRead(P4JClient, String[]) | 
| 3. | send | C2S | String   | choice → input[random]	   | clientWrite(P4JClient) → String | 
| 4. | read | C2S | String   | choice					   | serverRead(ServerClient, String) | 

And the System.out output (for a single client):
```
(Server): Server bound						     // <- bind
(Server): Server listening and accepting clients // <- setAccepting

(Client): Client bound			     // <- bind
(Client): Client connected		     // <- connect
(Server): Client connected to server // -> sendChoiceRequest

// S2C
(Server): Asked to client	 // <- serverWrite
(Server): true			     // the packet was sent successfully
(Client): Question received: // <- clientRead
(Client): [Dog, or, Cat]	 // <- clientRead

// C2S
(Client): Choice prepared: Dog	    // <- C2S_CatDogPacket constructor
(Client): Responding to server: Dog // <- clientWrite
(Server): Client answered: Dog	    // <- serverRead
```	

Closing the Server & Client:
```java
// We close the client before the server
client.close();
client.join();

server.close();
server.join();

System.out.println("Client: "+client.isAlive()+", "+client.getState()+" and "+client.getClientStatus());
System.out.println("Server: "+server.isAlive()+", "+server.getState()+" and "+server.getServerStatus());
```
