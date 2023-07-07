# Packets4J
A lightweights abstract socket & packet library.

### Example
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
// S2C
Asked to client
Question received:
["Cat", "or", "Dog"]

// C2S
Responding to server: {choice}
Client answered: {choice}
```	

Closing the Server & Client:
```
client.close();

server.close();
```

CodecManagers:
```

```
The `S2CPacket` and `C2SPacket` could be registered with the same id as long as there is an outgoing (serverside) and an ingoing (clientside) Packet that take the same king of Object as input.<br>
The server cannot send a S2CPacket<String[]> with id 0, when the clients awaits a S2CPacket<String> as id 0. However, C2SPacket<String> (serverside) with id 0 would be a valid as long as the C2SPacket (clientside) also awaits a <String> with id 0.<br>

This table represents a valid packet configuration:<br>
(S/C : Server/Client where the packet was registered)
| S/C | ID | TYPE | OBJECT |
|-----|----|------|--------|
|  S  | 0  | S2C  | send T1| 
|  C  | 0  | S2C  | read T1| 
|  C  | 0  | C2S  | send T2| 
|  S  | 0  | C2S  | read T2| 

This table represents a wrong packet configuration:
| S/C | ID | TYPE | OBJECT |
|-----|----|------|--------|
|  S  | 0  | S2C  | send T2| 
|  C  | 0  | S2C  | read T1| 
|  C  | 0  | C2S  | send T2| 
|  S  | 0  | C2S  | read T3| 

Implementing `S2CPacket` and `C2SPacket` in the same subclass for the client and the server ensures that the awaited Object is the same for the two.<br>

EncryptionManager:
```

```

CompressionManager:
```

```

# Packages:
- [x] Codec
	- [x] CodecManager
	- [x] encoder
		- [x] Encoder<T>
		- [x] StringEncoder
		- [x] DoubleEncoder
		- [x] FloatEncoder
		- [x] IntegerEncoder
		- [x] CharacterEncoder
		- [x] MapEncoder
		- [x] ArrayEncoder
		- [x] ShortEncoder
		- [x] LongEncoder
	- [x] decoder
		- [x] Decoder<T>
		- [x] StringDecoder
		- [x] DoubleDecoder
		- [x] FloatDecoder
		- [x] IntegerDecoder
		- [x] CharacterDecoder
		- [x] MapDecoder
		- [x] ArrayDecoder
		- [x] ShortDecoder
		- [x] LongDecoder
- [x] crypt
	- [x] EncryptionManager
	- [x] encryptor
		- [x] Encryptor
		- [x] RawEncryptor
		- [x] AESEncryptor
	- [x] decryptor
		- [x] RawDecryptor
		- [x] AESDecryptor
- [ ] compress
	- [ ] CompressionManager
	- [ ] compressor
		- [x] Compressor
		- [x] RawCompressor
		- [ ] ZstdCompressor
		- [ ] LZOCompressor
		- [x] SnappyCompressor
	- [ ] decompressor
		- [x] Decompressor
		- [x] RawDecompressor
		- [ ] ZstdDecompressor
		- [ ] LZODecompressor
		- [x] SnappyDecompressor
- [x] packets
	- [x] PacketManager
	- [x] Packet
	- [x] s2c
		- [x] S2CPacket
	- [x] c2s
		- [x] C2SPacket
- [x] socket
	- [x] P4JInstance
	- [x] client
		- [x] P4JClient
		- [x] ClientServer
		- [x] ClientStatus
	- [x] server
		- [x] P4JServer
		- [x] ServerClient
		- [x] ServerStatus
