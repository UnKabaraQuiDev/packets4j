# Packets4J
A lightweights abstract TCP/IP socket & packet library.

### Packets
A `Packet<T>` is an interface that represents a packet containing data of type `T`. The generic parameter `T` specifies the type of values that the packet encode and decode.<br>
When working with packets, the T parameter allows you to define the specific type of data that the packet encapsulates. It provides a way to make the packet interface more flexible and reusable, as you can use different types for different packets.<br>
The interface Packet<T> implements 2 subinterfaces: `S2CPacket<T>` and `C2SPacket<T>`, to separate `Server→Client` and `Client→Server` communication. <br>
The packet construction pattern consists of several elements that are used to construct a packet:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
| int  | 4B     |LENGTH| The total Packet length, excluding the 4 bytes used to store the length itself.|
| int  | 4B     | ID   | The id of the Packet.|
| x    |Variable| DATA | The [data block(s)](#data-blocks) included in the packet.|

#### Data Blocks
The construction of a data block varies depending of the generic argument passed `T`, basic data blocks are defined as follows:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B     |HEADER| The header used to decode the following data, see [CodecManager](#codecmanager).|
| x    |variable| DATA | The data of the block.|

This definition is valable for generic types such as: Byte, Short, Integer, Double, Float, Long and Character, because their size is known. The header can be omitted in some cases, such as in arrays or maps where multiple following elements use the same header and is specified in the parent data block<br>
Data blocks for types with variable size such as String, Arrays, Lists and Maps have a different construction:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B     |HEADER| The header used to decode the following data, see [CodecManager](#codecmanager).|
|  int | 4B     |LENGTH| The length of the following data, see [CodecManager](#codecmanager).|
| x    |variable| DATA | The data of the block.|

Data blocks can be concatonated inside each other, for example a String[] (String array) is described this way:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B     |HEADER| The Array header.|
|int   | 4B     |LENGTH| The count of elements contained int the array.|
|short | 2B     |HEADER| The String header.|
| x    |variable| DATA | The data of the block.|

This specific data is constructed this way:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|  int | 4B     |LENGTH| The length of the following String.|
| x    | LENGTH | DATA | The bytes of the String.|

Note that the Header is dropped because it is specified after the Array's length; the same decoder is used for all of the elements contained in the array.


### CodecManager
The `CodecManager` class is responsible for managing the encoding and decoding of objects into and from `ByteBuffer` representations. It maintains a collection of registered encoders and decoders and provides methods to access and utilize them.
1. `void register(Decoder d, short header)`: This method registers a decoder with a specified header value.<br>`register(Encoder d, short header)`: This method registers an encoder with a specified header value.
2. `void register(Encoder e, Decoder d, short header)`: A convenience method to registers both an encoder and a decoder with the same header value.
3. `static CodecManager base()`: This static factory method creates and initializes a CodecManager instance with a set of base encoders and decoders for basic types such as byte, short, integer, double, float, long, character, string, array, and map. It returns the initialized CodecManager instance.

#### Encoder<T>
A `Encoder<T>` is used to encode an Object `T` to a `ByteBuffer`.
1. `ByteBuffer encoder(boolean head, T object)`: The `boolean head` specifies if the header should be included in the output. Returns the encoded object in a ByteBuffer;
2. `int estimateSize(boolean head, T obj)`: The `boolean head` specifies if the header should be included in the estimation, for some objects the size cannot be determined and will return -1 or 2 if the header is included.
3. `boolean confirmType(Object object)`: Returns true if the input object is an instance of the awaited type.

#### Decoder<T>
A `Decoder<T>` is used to decode a ByteBuffer input to the specified object `T`.
1. `T decode(boolean head, ByteBuffer input)  throws DecoderNotCompatibleException`: The `boolean head` specifies if the header should be verified, throws a `DecoderNotCompatibleException` if it isn't. Returns the decoded object from a ByteBuffer;

#### Common Methods for Decoders and Encoders
1. `CodecManager codecManager()`: Returns the [CodecManager](#codecmanager) which the Decoder is registered to.
2. `short header()`: Returns the [Header](#data-blocks) which the Decoder is registered to.
3. `Class<?> type()`: Returns the Class which the Decoder is registered to.
4. `void defaultRegister() throws IllegalArgumentException`: Verifies if the Decoder/Encoder was already registered, if it is it throws an `IllegalArgumentException`


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
The server cannot send a `S2CPacket<String[]>` with id 0, when the clients awaits a `S2CPacket<String>` as id 0. However, `C2SPacket<String>` (serverside) with id 0 would be a valid as long as the `C2SPacket` (clientside) also awaits a `<String>` with id 0.<br>

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
		- [x] StringEncodereceives this packet from the connected server
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
	public String[] serverWrite(ServerClient client) {
		System.out.println("Asked to client");
		return new String[] {"Dog", "or", "Cat"};
	}
}

// This class is used to manage the communication between the Client → Server
// It describes how to handle the received String and what value to send
public class C2S_CatDogPacket implements C2SPacket<String> {
	String choice;

	// A constructor with no argument is needed or a PacketInstanceException will be thrown
	public C2S_CatDogPacket() {}
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
		sendChoiceRequest(client); // See "Send Packets"
	}
};

// Register incoming and outdoing packets
// Because S2C packet takes a String[] and C2S packet takes a String
// We can't use the same id, because the classes aren't equal
server.getPackets().register(C2S_CatDogPacket.class, 1);
server.getPackets().register(S2C_CatDogPacket.class, 2);

// Bind to the local address and port
server.bind(new InetSocketAddress(8090));
System.out.println("Server bound");

// Set as listening and accepting clients
server.setAccepting();
System.out.println("Server listening and accepting clients");
```

Create a Client:
```
CodecManager clientCodec = CodecManager.base();
EncryptionManager clientEncryption = EncryptionManager.raw();
CompressionManager clientCompression = CompressionManager.raw();
P4JClient client = new P4JClient(clientCodec, clientEncryption, clientCompression);

// Same as the Server
client.getPackets().register(C2S_CatDogPacket.class, 1);
client.registerPacket(S2C_CatDogPacket.class, 2);

// Bind without any argument takes a free port, a specific port can be passed as argument
client.bind();
System.out.println("Client bound");
```

Connect the Client:
```
// Connect to the server
client.connect(InetAddress.getLocalHost(), 8090);
System.out.println("Client connected");
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
The server cannot send a `S2CPacket<String[]>` with id 0, when the clients awaits a `S2CPacket<String>` as id 0. However, `C2SPacket<String>` (serverside) with id 0 would be a valid as long as the `C2SPacket` (clientside) also awaits a `<String>` with id 0.<br>

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
