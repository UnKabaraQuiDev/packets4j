# Packets4J
A lightweight abstract TCP/IP socket & packet library.

------

## Content
- [Content](#content)
- [Dependencies](#dependencies)
- [Packets](#packets)
	- [Data Blocks](#data-blocks)
	- [Packet Build Order](#packet-build-order)
- [CodecManager](#codecmanager)

- [EncryptionManager](#encryptionmanager)
	- [Encryption](#encryption)
	- [Decryption](#decryption)
- [CompressionManager](#compressionmanager)
	- [Compression](#compression)
	- [Decompression](#decompression)

- [Events](#events)

- [Examples](#examples)
- [Compiling](#compiling)

------

## Dependencies
This project uses the following dependencies:
- [JBCodec](https://github.com/Poucy113/jbcodec): Java Byte Codec >= v1.0

------

## Packets
A `Packet<T>` is an interface that represents a packet containing data of type `T`. The generic parameter `T` specifies the type of values that the packet encode and decode.<br>
When working with packets, the T parameter allows you to define the specific type of data that the packet encapsulates. It provides a way to make the packet interface more flexible and reusable, as you can use different types for different packets.<br>
The packet construction pattern consists of several elements that are used to construct a packet:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
| int  | 4B	 |LENGTH| The total Packet length, excluding the 4 bytes used to store the length itself.|
| int  | 4B	 | ID   | The id of the Packet.|
| x	|Variable| DATA | The [data block(s)](#data-blocks) included in the packet.|

The interface Packet<T> implements 2 subinterfaces: `S2CPacket<T>` and `C2SPacket<T>`, to separate `Server→Client` and `Client→Server` communication. <br>
The `S2CPacket` and `C2SPacket` could be registered with the same id as long as there is an outgoing (serverside) and an ingoing (clientside) Packet that take the same type of Object as input.<br>
The server cannot send a `S2CPacket<Object[]>` with id 0, when the clients awaits a `S2CPacket<String>` as id 0. However, `C2SPacket<String>` (serverside) with id 0 would be a valid as long as the `C2SPacket` (clientside) also awaits a `<String>` with id 0.<br>

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

#### Data Blocks
The construction of a data block varies depending of the generic argument passed `T`, basic data blocks are defined as follows:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B	 |HEADER| The header used to decode the following data, see [CodecManager](#codecmanager).|
| x	|variable| DATA | The data of the block.|

This definition is valable for generic types such as: Byte, Short, Integer, Double, Float, Long and Character, because their size is known. The header can be omitted in some cases, such as in arrays or maps where multiple following elements use the same header which is specified in the parent data block<br><br>
Data blocks for types with variable size such as String, Arrays, Lists and Maps have a different construction:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B	 |HEADER| The header used to decode the following data, see [CodecManager](#codecmanager).|
|  int | 4B	 |LENGTH| The length of the following data, see [CodecManager](#codecmanager).|
| x	|variable| DATA | The data of the block.|

Data blocks can be concatonated inside each other, for example a String[] (String array) is described this way:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|short | 2B	 |HEADER| The Array header.|
|int   | 4B	 |LENGTH| The count of elements contained int the array.|
|short | 2B	 |HEADER| The String header.|
| x	|variable| DATA | The data of the block.|

This specific data is constructed this way:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
|  int | 4B	 |LENGTH| The length of the following String.|
| x	| LENGTH | DATA | The bytes of the String.|

Note that the Header is dropped because it is specified after the Array's length; the same decoder is used for all of the elements contained in the array.

#### Packet Build Order
Data Blocks generated by the [CodecManager](#codecmanager), are handed to the [EncryptionManager](#encryptionmanager) and the [CompressionManager](#compressionmanager).<br>These operations follow a specific order for writing the packet:
| NAME | RETURNS | DESCRIPTION |
|------|---------|-------------|
|`new Packet<T>()`| Packet | New packet instance created.|
|`Packet#xWrite(x)`| Object | The write method of the packet is called. |
|`CodecManager#encode(Object)`| ByteBuffer | The packet's content gets encoded by the corresponding `Encoder<T>`.|
|`EncryptionManager#encoder(ByteBuffer)`| ByteBuffer | The ByteBuffer gets encrypted using the registered `Encryptor`.|
|`CompressionManager#compress(ByteBuffer)`| ByteBuffer | The ByteBuffer gets compressed using the registered `Compressor`.|
|ADD LENGTH, ID| ByteBuffer | The length of the packet and it's ID is added.|
|`write(ByteBuffer)`| boolean | Writes the `ByteBuffer` to the `OutputStream`. Returns true if succeeded.|

To read an incoming packet, the same operations are done in reverse.<br>Data Blocks are handed to the [CompressionManager](#compressionmanager) and the [EncryptionManager](#encryptionmanager):
| NAME | RETURNS | DESCRIPTION |
|------|---------|-------------|
|`read(ByteBuffer (4))`| int | Reads the input for 4 bytes. This represents the LENGTH of the following packet.|
|`read(ByteBuffer (LENGTH))`| int | Reads the input for LENGTH bytes.|
|PARSE ID| ByteBuffer | The 4 first bytes of the data represent the ID of the packet.|
|`CompressionManager#decompress(ByteBuffer)`| ByteBuffer | The ByteBuffer gets decompressed using the registered `Decompressor`.|
|`EncryptionManager#decrypt(ByteBuffer)`| ByteBuffer | The ByteBuffer gets decrypted using the registered `Decryptor`.|
|`CodecManager#decode(Packet)`| ByteBuffer | The packet's content gets decoded by the corresponding `Decoder<T>`.|
|`PacketManager#packetInstance(ID)`| Packet | New packet instance is created using it's id.|
|`Packet#xRead(x, obj)`| void | The packet is read by the receiver. |

------

## CodecManager
See [JBCodec's CodecManager](https://github.com/Poucy113/jbcodec#codecmanager).

------

## EncryptionManager
The `EncryptionManager` class is responsible for managing the encryption and decryption of the input and output `ByteBuffers`.
1. `Encryptor getEncryptor()`, `Decryptor getDecryptor()`: Getters for Encryptor/Decryptor.
2. `void setEncryptor(Encryptor e)`, `void setDecryptor(Decryptor d)`: Setters for Encryptor/Decryptor.
3. `static EncryptionManager raw()`: This static factory method creates and initializes a `EncryptionManager` instance with `RawDecryptor` and `RawEncryptor`, this does not encrypt.
4. `static EncryptionManager aes(byte[] key)`: This static factory method creates and initializes a `EncryptionManager` instance using the AES symmetric key algorithm.

------

## CompressionManager
The `CompressionManager` class is responsible for managing the compressing and decompressing the input and output `ByteBuffers`.
1. `Compressor getCompressor()`, `Decompressor getDecompressor()`: Getters for Compressor/Decompressor.
2. `void setCompressor(Compressor e)`, `void setDecompressor(Decompressor d)`: Setters for Compressor/Decompressor.
3. `static CompressionManager raw()`: This static factory method creates and initializes a `CompressionManager` instance with `RawCompressor` and `RawDecompressor`, this does not compresses.

------

## Events
There are 2 default EventManagers:
- `AsyncEventManager` handles events asyncronously, in a separate thread pool.
- `EventManager` handles events syncronously, default EventManager.

**Changing the default EventManager**:<br>
*The default is SyncEventManager*<br>
`P4JServer.setEventManager(<consumer>)`<br>
`P4JCient.setEventManager(<consumer>)`<br>

**Adding an EventListener**:<br>
`P4JServer.getEventManager().register(<listener>)`<br>
`P4JClient.getEventManager().register(<listener>)`<br>

**EventListener usage:**<br>
- The `@ListenerPriority` (optional) annotation is used to change the listener's priority *(higher = executed earlier, default is 0)*
- The `@EventHandler` annotation is used to mark methods to handle the event specified by the first parameter.
*Example*:
```java
@ListenerPriority(priority = 10)
public class MyEventListener implements EventListener {
	@EventHandler
	public void onClientConnect(ClientConnectedEvent event) {
		// do something
	}
	// the name doesn't matter
	@EventHandler
	public void kjwdsfkjhsf(P4JEvent event) {
		// do something
	}
}
````

There are multiple events (*implementing `P4JEvent`*):
- `ClientConnectedEvent(P4JClientInstance, P4JServerInstance)`: When a client connects to a server.<br>
Server side: ServerClient -> P4JServer<br>
Client side: P4JClient -> ClientServer
- `S2CReadPacketEvent(P4JInstance, Packet, Class<Packet>, int, Throwable, boolean)`: When a client reads an incoming packet from the server.
- `S2CWritePacketEvent(P4JClientInstance, Packet, Class<Packet>, int, Throwable, boolean)`: When a server writes an outgoing packet to the client.
- `C2SReadPacketEvent(P4JClientInstance, Packet, Class<Packet>, int, Throwable, boolean)`: When a server reads an incoming packet from the client.
- `C2SWritePacketEvent(P4JClientInstance, Packet, Class<Packet>, int, Throwable, boolean)`: When a client writes an outgoing packet to the server.
- `ClientDisconnectedEvent(ClosedChannelException, P4JClientInstance)`: When a client connection gets closed.


------

## Examples
See [Cat Dog Question Example](/src/main/examples/catdog/CatDogExample.md)

------

## Compiling
To compile use maven, implemented options are: `test`, `package`, `install` *& defaults*
