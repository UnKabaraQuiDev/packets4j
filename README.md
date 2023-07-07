# Packets4J
A lightweights abstract TCP/IP socket & packet library.

## Packets
A `Packet<T>` is an interface that represents a packet containing data of type `T`. The generic parameter `T` specifies the type of values that the packet encode and decode.<br>
When working with packets, the T parameter allows you to define the specific type of data that the packet encapsulates. It provides a way to make the packet interface more flexible and reusable, as you can use different types for different packets.<br>
The packet construction pattern consists of several elements that are used to construct a packet:
| TYPE | LENGTH | NAME | DESCRIPTION |
|------|--------|------|-------------|
| int  | 4B     |LENGTH| The total Packet length, excluding the 4 bytes used to store the length itself.|
| int  | 4B     | ID   | The id of the Packet.|
| x    |Variable| DATA | The [data block(s)](#data-blocks) included in the packet.|

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


## CodecManager
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


## EncryptionManager
The `EncryptionManager` class is responsible for managing the encryption and decryption of the input and output `ByteBuffers`.
1. `void register(Decoder d, short header)`: This method registers a decoder with a specified header value.<br>`register(Encoder d, short header)`: This method registers an encoder with a specified header value.
2. `void register(Encoder e, Decoder d, short header)`: A convenience method to registers both an encoder and a decoder with the same header value.
3. `static CodecManager base()`: This static factory method creates and initializes a CodecManager instance with a set of base encoders and decoders for basic types such as byte, short, integer, double, float, long, character, string, array, and map. It returns the initialized CodecManager instance.


## Examples
See [Cat Dog Question Example](src/lu/pcy113/p4j/examples/CatDogExample.md)

### EncryptionManager:
```

```

### CompressionManager:
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
