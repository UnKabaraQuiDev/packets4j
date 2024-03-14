package lu.pcy113.p4j.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.EventQueueConsumer;
import lu.pcy113.p4j.packets.PacketManager;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JInstance;
import lu.pcy113.p4j.socket.P4JServerInstance;

public class P4JServer extends Thread implements P4JInstance, P4JServerInstance {

	private ServerStatus serverStatus = ServerStatus.PRE;

	public EventQueueConsumer events = EventQueueConsumer.IGNORE;

	private CodecManager codec;
	private EncryptionManager encryption;
	private CompressionManager compression;
	private PacketManager packets = new PacketManager(this);
	private ClientManager clientManager;

	private InetSocketAddress localInetSocketAddress;

	private ServerSocketChannel serverSocketChannel;
	private Selector serverSocketSelector;
	
	/**
	 * Default constructor for a P4JServer, creates a default {@link ClientManager} bound to this server instance.
	 * 
	 * @param CodecManager the server codec manager 
	 * @param EntryptionManager the server encryption manager
	 * @param CompressionManager the server compression manager
	 */
	public P4JServer(CodecManager cm, EncryptionManager em, CompressionManager com) {
		this.codec = cm;
		this.encryption = em;
		this.compression = com;
		this.clientManager = new ClientManager(this);
	}

	/*public P4JServer(CodecManager cm, EncryptionManager em, CompressionManager com, ClientManager clientManager) {
		this.codec = cm;
		this.encryption = em;
		this.compression = com;
		this.clientManager = clientManager;
	}*/
	
	/**
	 * Binds the current server to the local address.
	 * 
	 * @param InetSocketAddress the local address to bind to
	 * @throws IOException if the {@link ServerSocketChannel} or {@link Selector} cannot be opened or bound
	 * @throws P4JServerException if the server is already bound
	 */
	public void bind(InetSocketAddress isa) throws IOException {
		if(!serverStatus.equals(ServerStatus.PRE)) {
			throw new P4JServerException("Server already bound");
		}
		
		serverSocketSelector = Selector.open();
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(isa);
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(serverSocketSelector, SelectionKey.OP_ACCEPT);
		serverStatus = ServerStatus.BOUND;

		this.localInetSocketAddress = new InetSocketAddress(serverSocketChannel.socket().getInetAddress(), serverSocketChannel.socket().getLocalPort());
		super.setName("P4JServer@" + localInetSocketAddress.getHostString() + ":" + localInetSocketAddress.getPort());

		// super.start();
	}

	public void run() {
		try {
			while (serverStatus.equals(ServerStatus.ACCEPTING)) {
				serverSocketSelector.select();

				Set<SelectionKey> selectedKeys = serverSocketSelector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();

					if (key.isAcceptable()) {
						// Accept a new client connection
						ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
						SocketChannel clientChannel = serverChannel.accept();
						clientChannel.configureBlocking(false);

						// Register the client socket channel with the selector for reading
						clientChannel.register(serverSocketSelector, SelectionKey.OP_READ);

						clientManager.register(clientChannel);
					} else if (key.isReadable()) {
						// Read data from a client socket channel
						SocketChannel clientChannel = (SocketChannel) key.channel();
						clientManager.get(clientChannel).read();

					} else if (key.isWritable()) {
						SocketChannel clientChannel = (SocketChannel) key.channel();
						clientChannel.socket().getOutputStream().flush();
						//System.out.println("server#read: flushed");
					}

					keyIterator.remove();
				}
			}
		} catch (IOException e) {
			handleException("run", e);
		}
	}

	/**
	 * Handles the given exception in this server instance.<br>
	 * It is strongly encouraged to override this method.
	 * 
	 * @param String the message (the context) ("run", "close")
	 * @param Exception the exception
	 */
	protected void handleException(String msg, Exception e) {
		System.err.println(getClass().getName() + "/" + localInetSocketAddress + "> " + msg + " ::");
		e.printStackTrace(System.err);
	}

	/**
	 * Sends the packet to all the connected clients.
	 * 
	 * @param S2CPacket the packet to send
	 */
	public void broadcast(S2CPacket packet) {
		for (ServerClient sc : clientManager.getAllClients()) {
			sc.write(packet);
		}
	}

	/**
	 * Sets the server socket in client accept mode.<br>
	 * The server will accept all future incoming client connections.
	 * 
	 * @throws P4JServerException if the server socket is closed
	 */
	public void setAccepting() {
		if (serverStatus.equals(ServerStatus.CLOSED))
			throw new P4JServerException("Cannot set closed server socket in client accept mode.");
		serverStatus = ServerStatus.ACCEPTING;

		if (!super.isAlive()) {
			super.start();
		}
	}

	/**
	 * Closes the server socket.<br>
	 * The server will no longer accept new client connections, all clients will be forcefully disconnected and the local port is released.
	 * 
	 * @throws P4JServerException if the server socket is already closed
	 */
	public void close() {
		if (serverStatus.equals(ServerStatus.CLOSED) || serverStatus.equals(ServerStatus.PRE))
			throw new P4JServerException("Cannot close not started server socket.");

		try {
			serverSocketChannel.close();
			serverStatus = ServerStatus.CLOSED;
		} catch (IOException e) {
			handleException("close", e);
		}
	}

	/**
	 * Sets the server socket in client refuse mode.<br>
	 * The server will refuse all future incoming client connections but keep current connections alive.
	 * 
	 * @throws P4JServerException if the server socket is closed.
	 */
	public void setRefusing() {
		if (serverStatus.equals(ServerStatus.CLOSED))
			throw new P4JServerException("Cannot set closed server socket in client refuse mode.");
		this.serverStatus = ServerStatus.REFUSING;
	}

	public void registerPacket(Class<?> p, int id) {
		packets.register(p, id);
	}

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	public InetSocketAddress getLocalInetSocketAddress() {
		return localInetSocketAddress;
	}

	public Collection<ServerClient> getConnectedClients() {
		return clientManager.getAllClients();
	}

	/**
	 * @return the local port bound to the server or -1 if the server is closed
	 */
	public int getPort() {
		return (serverSocketChannel != null && serverSocketChannel.socket() != null ? serverSocketChannel.socket().getLocalPort() : -1);
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

	public ClientManager getClientManager() {
		return clientManager;
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

	public void setClientManager(ClientManager clientManager) {
		this.clientManager = clientManager;
	}

	public void setEventQueueConsumer(EventQueueConsumer events) {
		this.events = events;
	}

}