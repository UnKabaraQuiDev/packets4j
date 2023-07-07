Create a Packet:
```
// This class is used to manage the communication between the Server → Client
// It describes how to handle the received String[] and what value to send
public class S2C_CatDogPacket implements S2CPacket<String[]> {
	
	// Gets called when a Client receives this packet from the connected server
	public void clientRead(P4JClient client, String[] input) {
		System.out.println("Question received: ");
		System.out.println(Arrays.toString(input));
		Random r = new Random();
		int choiceIndex = r.nextInt(input.length);
		client.write(new C2S_CatDogPacket(input[choiceIndex]));
	}

	// Gets called when using ServerClient.write(new S2C_CatDogPacket())
	// Returns the value to be sent
	public String[] serverWrite(ServerClient client) {
		System.out.println("Asked to client");
		return new String[] {"Dog", "or", "Cat"};
	}
}

// This class is used to manage the communication between the Client → Server
// It describes how to handle the received String and what value to send
public class C2S_CatDogPacket implements C2SPacket<String> {
	String choice;

	public C2S_CatDogPacket(String choice) {
		this.choice = choice;
	}

	// Gets called when using P4JClient.write(new C2S_CatDogPacket())
	// Returns the value to be sent
	public String clientWrite(P4JClient client) {
		System.out.println("Responding to server: "+choice);
		return this.choice;
	}

	// Gets called when a Server receives this packet from a connected Client
    public void serverRead(ServerClient sclient, String obj)() {
		System.out.println("Client answered: "+obj);
	}
}
```
The `S2CPacket` and `C2SPacket` interfaces can be implementing the same Object.

Create a Server:
```
CodecManager serverCodec = CodecManager.base();
EncryptionManager serverEncryption = EncryptionManager.raw();
CompressionManager serverCompression = CompressionManager.raw();
P4JServer server = new P4JServer(serverCodec, serverEncryption, serverCompression) {
	@Override
	public void clientConnected(ServerClient client) {
		sendChoiceRequest(); // See "Send Packets"
	}
};

// Register incoming and outdoing packets
// Because S2C packet takes a String[] and C2S packet takes a String
// We can't use the same id, because the classes aren't equal
server.getPackets().register(C2S_CatDogPacket.class, 1);
server.getPackets().register(S2C_CatDogPacket.class, 2);

// Bind to the local port
server.bind(8090);

// Set as listening and accepting clients
server.setAccepting();
```

Create a Client:
```
CodecManager clientCodec = CodecManager.base();
EncryptionManager clientEncryption = EncryptionManager.raw();
CompressionManager clientCompression = CompressionManager.raw();
P4JClient client = new P4JClient(clientCodec, clientEncryption, clientCompression);

// Same as the Server
client.getPackets().register(C2S_CatDogPacket.class, 1);
client.getPackets().register(S2C_CatDogPacket.class, 2);

// Bind without any argument takes a free port, a specific port can be passed as argument
client.bind();
```

Connect the Client:
```
// Connect to the server
client.connect(InetAddress.getLocalHost(), 8090);
```

Send Packets:
```
// See "Create a Server"
private void sendChoiceRequest(ServerClient client) {
	System.out.println("Client connected to server");

	// Send a packet to the newly connected client
	client.write(new S2C_CatDogPacket());

	// OR
	
	// Broadcast a packet to all clients
	server.broadcast(new S2C_CatDogPacket());
}
```

In this example, the server-client packet exchange should look like this:
| ORDER | DIR | TYPE | OBJECT | VALUE | FUNCTION |
|------|-----|------|--------|-------|-----------|
| 1. | send | S2C | String[] | input → {"Cat", "or", "Dog"} | serverWrite(ServerClient) → String[] | 
| 2. | read | S2C | String[] | input                        | clientRead(P4JClient, String[]) | 
| 3. | send | C2S | String   | choice → input[random]       | clientWrite(P4JClient) → String | 
| 4. | read | C2S | String   | choice                       | serverRead(ServerClient, String) | 

And the System.out output (for a single client):
```
(Server): Server bound                           // bind
(Server): Server listening and accepting clients // setAccepting

(Client): Client bound               // bind
(Client): Client connected           // connect
(Server): Client connected to server // sendChoiceRequest

// S2C
(Server): Asked to client    // serverWrite
(Server): true               // the packet was sent successfully
(Client): Question received: // clientRead
(Client): [Dog, or, Cat]     // |

// C2S
(Client): Choice prepared: Dog      // constructor
(Client): Responding to server: Dog // clientWrite
(Server): Client answered: Dog      // serverRead
```	

Closing the Server & Client:
```
client.close();

server.close();
```
