package lu.pcy113.p4j.socket.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.util.function.Consumer;

import javax.net.SocketFactory;

import lu.pcy113.jbcodec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.C2SWritePacketEvent;
import lu.pcy113.p4j.events.ClientConnectedEvent;
import lu.pcy113.p4j.events.ClosedSocketEvent;
import lu.pcy113.p4j.events.P4JEvent;
import lu.pcy113.p4j.events.S2CReadPacketEvent;
import lu.pcy113.p4j.exceptions.P4JClientException;
import lu.pcy113.p4j.packets.PacketManager;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.P4JInstance;
import lu.pcy113.pclib.PCUtils;
import lu.pcy113.pclib.listener.EventDispatcher;
import lu.pcy113.pclib.listener.EventManager;
import lu.pcy113.pclib.listener.SyncEventManager;

/**
 * This class represents the client-side Client connecting to the server.
 * 
 * @author pcy113
 */
public class P4JClient extends Thread implements P4JInstance, P4JClientInstance, EventDispatcher, Closeable {

	private ClientStatus clientStatus = ClientStatus.PRE;

	private EventManager eventManager = new SyncEventManager();

	private CodecManager codec;
	private EncryptionManager encryption;
	private CompressionManager compression;
	private PacketManager packets = new PacketManager(this);

	private InetSocketAddress localInetSocketAddress;
	private Socket clientSocket;
	private InputStream inputStream;
	private OutputStream outputStream;

	private ClientServer clientServer;

	private Consumer<P4JClientException> exceptionConsumer = P4JClientException::printStackTrace;

	/**
	 * 
	 * @param CodecManager       the client codec manager
	 * @param EntryptionManager  the client encryption manager
	 * @param CompressionManager the client compression manager
	 */
	public P4JClient(CodecManager cm, EncryptionManager em, CompressionManager com) {
		this.codec = cm;
		this.encryption = em;
		this.compression = com;
	}

	/**
	 * Bind to a random available port on the local machine.
	 * 
	 * @throws IOException
	 */
	public void bind() throws IOException {
		bind(0);
	}

	/**
	 * Bind to the specified port on the local machine. If the port is 0, a random available port is chosen.
	 * 
	 * @param int the port to bind to
	 * @throws IOException if the {@link Socket} cannot be created or bound
	 */
	public void bind(int port) throws IOException {
		bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
	}

	/**
	 * Bind to the specified port on the local machine. If the port is 0, a random available port is chosen.
	 * 
	 * @param InetSocketAddress the local address to bind to
	 * @throws IOException if the {@link Socket} cannot be created or bound
	 */
	public void bind(InetSocketAddress isa) throws IOException {
		clientSocket = SocketFactory.getDefault().createSocket();
		clientSocket.bind(isa);
		clientStatus = ClientStatus.BOUND;

		this.localInetSocketAddress = new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getLocalPort());
		super.setName("P4JClient@" + localInetSocketAddress.getHostString() + ":" + localInetSocketAddress.getPort());
	}

	/**
	 * Connect to the specified address and port on the remote machine.
	 * 
	 * @param remote the remote address
	 * @param port   the remote port
	 * @throws IOException if the {@link Socket} cannot be connected
	 */
	public void connect(InetAddress remote, int port) throws IOException {
		if (!clientStatus.equals(ClientStatus.BOUND)) {
			throw new P4JClientException("Client not bound");
		}
		try {
			clientSocket.connect(new InetSocketAddress(remote, port), 5_000);
			clientSocket.setSoTimeout(200); // ms
			this.inputStream = clientSocket.getInputStream();
			this.outputStream = clientSocket.getOutputStream();

			clientStatus = ClientStatus.LISTENING;

			clientServer = new ClientServer(new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort()));

			if (!super.isAlive()) {
				super.start();
			}

			dispatchEvent(new ClientConnectedEvent(this, clientServer));
		} catch (SocketTimeoutException e) {
			close();
			throw new P4JClientException("Connection timed out", e);
		} catch (ConnectException e) {
			close();
			throw new P4JClientException("Connection refused", e);
		} catch (SocketException e) {
			close();
			throw new P4JClientException(e);
		} catch (IOException e) {
			close();
			throw new P4JClientException(e);
		} catch (IllegalStateException e) {
			close();
			throw new P4JClientException(e);
		}
	}

	/**
	 * Connect to the specified address and port on the remote machine.
	 * 
	 * @see {@link #connect(InetAddress, int)}
	 */
	public void connect(InetSocketAddress isa) throws IOException {
		this.connect(isa.getAddress(), isa.getPort());
	}

	@Override
	public void run() {
		while (clientStatus.equals(ClientStatus.LISTENING)) {
			read();
		}
		// clientStatus = ClientStatus.CLOSED;
	}

	public void read() {
		try {
			final byte[] bb = new byte[4];
			final int bytesRead = inputStream.read(bb);
			if (bytesRead == -1) {
				dispatchEvent(new ClosedSocketEvent(this));
				close();
				return;
			}
			if (bytesRead != 4) {
				return;
			}

			final int length = PCUtils.byteToInt(bb);

			final byte[] cc = new byte[length];
			if (inputStream.read(cc) != length) {
				return;
			}

			final ByteBuffer content = ByteBuffer.wrap(cc);
			final int id = content.getInt();

			read_handleRawPacket(id, content);
		} catch (NotYetConnectedException e) {
			handleException(new P4JClientException(e));
		} catch (ClosedByInterruptException e) {
			Thread.interrupted(); // clear interrupt flag
			// ignore because triggered in #close()
		} catch (ClosedChannelException e) {
			// ignore because triggered in #close()
		} catch (SocketException e) {
			handleException(new P4JClientException(e));
		} catch (SocketTimeoutException e) {
			// ignore, just return
		} catch (IOException e) {
			handleException(new P4JClientException(e));
		}
	}

	protected void read_handleRawPacket(int id, ByteBuffer content) {
		try {
			content = compression.decompress(content);
			content = encryption.decrypt(content);
			Object obj = codec.decode(content);

			S2CPacket packet = (S2CPacket) packets.packetInstance(id);

			packet.clientRead(this, obj);

			dispatchEvent(new S2CReadPacketEvent(this, packet, packets.getClass(id)));
		} catch (Exception e) {
			dispatchEvent(new S2CReadPacketEvent(this, id, e));
			handleException(new P4JClientException(e));
		}
	}

	public synchronized boolean write(C2SPacket packet) {
		try {
			Object obj = packet.clientWrite(this);
			ByteBuffer content = codec.encode(obj);
			content = encryption.encrypt(content);
			content = compression.compress(content);

			ByteBuffer bb = ByteBuffer.allocate(4 + 4 + content.capacity());
			bb.putInt(content.limit() + 4); // Add id length
			bb.putInt(packets.getId(packet.getClass()));
			bb.put(content);
			bb.flip();

			if (bb.hasArray()) {
				outputStream.write(bb.array());
			} else {
				outputStream.write(PCUtils.byteBufferToArray(bb));
			}

			outputStream.flush();

			dispatchEvent(new C2SWritePacketEvent(this, packet));

			return true;
		} catch (ClosedChannelException e) {
			dispatchEvent(new ClosedSocketEvent(e, this));
			close();
			return false;
		} catch (Exception e) {
			dispatchEvent(new C2SWritePacketEvent(this, packet, e));
			handleException(new P4JClientException(e));
			return false;
		}
	}

	/**
	 * Disconnects & closes the client socket<br>
	 * And dispatches a {@link ClosedSocketEvent}.
	 * 
	 * @see #close()
	 * @throws P4JClientException if the client isn't started
	 */
	public void disconnect() {
		close();
		dispatchEvent(new ClosedSocketEvent(this));
	}

	/**
	 * Closes the client socket.<br>
	 * The client' socket will be closed and the port will be released.<br>
	 * Doesn't dispatch a {@link ClosedSocketEvent}.
	 * 
	 * @see {@link #disconnect()}
	 * @throws P4JClientException if the client isn't started
	 */
	@Override
	public void close() {
		if (!clientStatus.equals(ClientStatus.LISTENING)) {
			clientStatus = ClientStatus.CLOSED;
			return;
		}

		try {
			clientStatus = ClientStatus.CLOSING;
			this.interrupt();
			if (clientSocket != null) {
				clientSocket.close();
			}
			clientStatus = ClientStatus.CLOSED;

			clientSocket = null;
			clientServer = null;
		} catch (Exception e) {
			handleException(new P4JClientException(e));
		}
	}

	private void handleException(P4JClientException e) {
		if (exceptionConsumer != null) {
			exceptionConsumer.accept(e);
		}
		close();
	}

	public void registerPacket(Class<?> p, int id) {
		packets.register(p, id);
	}

	public void dispatchEvent(P4JEvent event) {
		if (eventManager == null)
			return;

		eventManager.dispatch(event, this);
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

	/**
	 * @return the current port the client is connected to or -1 if it isn't bound
	 */
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

	public EventManager getEventManager() {
		return eventManager;
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

	public void setEventManager(EventManager eventManager) {
		this.eventManager = eventManager;
	}

	public Consumer<P4JClientException> getExceptionConsumer() {
		return exceptionConsumer;
	}

	public void setExceptionConsumer(Consumer<P4JClientException> exceptionConsumer) {
		this.exceptionConsumer = exceptionConsumer;
	}

}