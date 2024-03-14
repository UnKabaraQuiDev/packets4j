package lu.pcy113.p4j.socket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;

import javax.net.SocketFactory;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.jb.utils.ArrayUtils;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.ClientReadPacketEvent;
import lu.pcy113.p4j.events.ClientWritePacketEvent;
import lu.pcy113.p4j.events.ClosedSocketEvent;
import lu.pcy113.p4j.events.EventQueueConsumer;
import lu.pcy113.p4j.packets.PacketManager;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.P4JInstance;

public class P4JClient extends Thread implements P4JInstance, P4JClientInstance {

	private ClientStatus clientStatus = ClientStatus.PRE;

	public EventQueueConsumer events = EventQueueConsumer.IGNORE;

	private CodecManager codec;
	private EncryptionManager encryption;
	private CompressionManager compression;
	private PacketManager packets = new PacketManager(this);

	private InetSocketAddress localInetSocketAddress;
	private Socket clientSocket;
	private InputStream inputStream;
	private OutputStream outputStream;

	private ClientServer clientServer;

	public P4JClient(CodecManager cm, EncryptionManager em, CompressionManager com) {
		this.codec = cm;
		this.encryption = em;
		this.compression = com;
	}

	public void bind() throws IOException {
		bind(0);
	}

	public void bind(int port) throws IOException {
		clientSocket = SocketFactory.getDefault().createSocket();
		clientSocket.bind(new InetSocketAddress(port));
		clientStatus = ClientStatus.OPEN;

		this.localInetSocketAddress = new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getLocalPort());
		super.setName("P4JClient@" + localInetSocketAddress.getHostString() + ":" + localInetSocketAddress.getPort());
	}

	public void connect(InetAddress remote, int port) throws IOException {
		// clientSocket.configureBlocking(true);
		clientSocket.connect(new InetSocketAddress(remote, port));
		clientSocket.setSoTimeout(200); // ms
		// clientSocketChannel.socket().setTcpNoDelay(true);
		this.inputStream = clientSocket.getInputStream();
		this.outputStream = clientSocket.getOutputStream();

		clientStatus = ClientStatus.LISTENING;

		clientServer = new ClientServer(new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort()));

		if (!super.isAlive()) {
			super.start();
		}

		events.handle(new ClientConnectedEvent(this, clientServer));
	}

	public void connect(InetSocketAddress isa) throws IOException {
		this.connect(isa.getAddress(), isa.getPort());
	}

	@Override
	public void run() {
		while (clientStatus.equals(ClientStatus.LISTENING)) {
			read();
		}
		clientStatus = ClientStatus.CLOSED;
	}

	public void read() {
		try {
			byte[] bb = new byte[4];
			if ( inputStream.read(bb) != 4) {
				return;
			}

			int length = ArrayUtils.byteToInt(bb);

			byte[] cc = new byte[length];
			if (inputStream.read(cc) != length) {
				return;
			}

			ByteBuffer content = ByteBuffer.wrap(cc);
			int id = content.getInt();

			read_handleRawPacket(id, content);
		} catch (NotYetConnectedException e) {
			handleException("read", e);
		} catch (ClosedByInterruptException e) {
			// ignore because triggered in #close()
		} catch (ClosedChannelException e) {
			// ignore
		} catch (SocketException e) {
			if (clientStatus.equals(ClientStatus.LISTENING)) {
				handleException("read", e);
			}
		} catch (SocketTimeoutException e) {
			// ignore, just return
		} catch (IOException e) {
			if (clientStatus.equals(ClientStatus.LISTENING)) {
				handleException("read", e);
			}
		}
	}

	protected void read_handleRawPacket(int id, ByteBuffer content) {
		try {
			content = compression.decompress(content);
			content = encryption.decrypt(content);
			Object obj = codec.decode(content);

			S2CPacket packet = (S2CPacket) packets.packetInstance(id);

			events.handle(new ClientReadPacketEvent(this, packet, packets.getClass(id)));

			packet.clientRead(this, obj);
		} catch (Exception e) {
			events.handle(new ClientReadPacketEvent(this, id, e));
			handleException("read_handleRawPacket", e);
		}
	}

	public synchronized boolean write(C2SPacket packet) {
		try {
			Object obj = packet.clientWrite(this);
			ByteBuffer content = codec.encode(obj);
			// System.err.println("client sent: " +
			// ArrayUtils.byteBufferToHexString(content));
			content = encryption.encrypt(content);
			content = compression.compress(content);

			ByteBuffer bb = ByteBuffer.allocate(4 + 4 + content.capacity());
			bb.putInt(content.limit() + 4); // Add id length
			bb.putInt(packets.getId(packet.getClass()));
			bb.put(content);
			bb.flip();

			// System.out.println("client#write: "+ArrayUtils.byteBufferToHexString(bb));

			if (bb.hasArray()) {
				outputStream.write(bb.array());
			} else {
				outputStream.write(ArrayUtils.byteBufferToArray(bb));
			}
			
			outputStream.flush();

			events.handle(new ClientWritePacketEvent(this, packet));

			return true;
		} catch (Exception e) {
			events.handle(new ClientWritePacketEvent(this, packet, e));
			handleException("write", e);
			return false;
		}
	}

	public void close() {
		if (!clientStatus.equals(ClientStatus.LISTENING))
			throw new P4JClientException("Cannot close not started client socket.");

		try {
			clientStatus = ClientStatus.CLOSING;
			// this.interrupt(); // No need to interrupt because will stop reading after
			// soTimeout
			clientSocket.close();
			clientStatus = ClientStatus.CLOSED;

			clientSocket = null;
			clientServer = null;

			events.handle(new ClosedSocketEvent(null, this));
		} catch (IOException e) {
			handleException("close", e);
		}
	}

	protected void handleException(String msg, Exception e) {
		System.err.println(getClass().getName() + "/" + localInetSocketAddress + "> " + msg + " ::");
		e.printStackTrace(System.err);
		close();
	}

	public void registerPacket(Class<?> p, int id) {
		packets.register(p, id);
	}

	public ClientStatus getClientStatus() {
		return clientStatus;
	}

	public InetSocketAddress getLocalInetSocketAddress() {
		return localInetSocketAddress;
	}

	public ClientServer getClientServer() {
		return clientServer;
	}

	public int getPort() {
		return (clientSocket != null ? clientSocket.getLocalPort() : -1);
	}

	public CodecManager getCodec() {
		return codec;
	}

	public EncryptionManager getEncryption() {
		return encryption;
	}

	public CompressionManager getCompression() {
		return compression;
	}

	public PacketManager getPackets() {
		return packets;
	}

	public EventQueueConsumer getEventQueueConsumer() {
		return events;
	}

	public void setCodec(CodecManager codec) {
		this.codec = codec;
	}

	public void setEncryption(EncryptionManager encryption) {
		this.encryption = encryption;
	}

	public void setCompression(CompressionManager compression) {
		this.compression = compression;
	}

	public void setPackets(PacketManager packets) {
		this.packets = packets;
	}

	public void setEventQueueConsumer(EventQueueConsumer events) {
		this.events = events;
	}

}