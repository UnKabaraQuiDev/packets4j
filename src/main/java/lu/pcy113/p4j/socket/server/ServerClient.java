package lu.pcy113.p4j.socket.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import lu.pcy113.p4j.events.C2SReadPacketEvent;
import lu.pcy113.p4j.events.ClosedSocketEvent;
import lu.pcy113.p4j.events.S2CWritePacketEvent;
import lu.pcy113.p4j.exceptions.P4JClientException;
import lu.pcy113.p4j.exceptions.P4JServerClientException;
import lu.pcy113.p4j.packets.UnknownPacketException;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JClientInstance;

/**
 * Represents a P4JServer's client. This is a wrapper around a {@link SocketChannel} that represents a client connected to the server, on the server-side.
 */
public class ServerClient implements P4JClientInstance, Closeable {

	private ServerClientStatus serverClientStatus = ServerClientStatus.PRE;

	private UUID uuid;
	private P4JServer server;

	private SocketChannel socketChannel;

	private Consumer<P4JServerClientException> exceptionConsumer = P4JServerClientException::printStackTrace;

	public ServerClient(SocketChannel sc, P4JServer server) {
		this.socketChannel = sc;
		this.server = server;

		this.uuid = UUID.randomUUID();

		this.serverClientStatus = ServerClientStatus.LISTENING;
	}

	public synchronized void read() {
		try {
			final ByteBuffer bb = ByteBuffer.allocateDirect(4);
			final int bytesRead = socketChannel.read(bb);
			if (bytesRead == -1) {
				server.dispatchEvent(new ClosedSocketEvent(this));
				close();
				return;
			}

			if (bytesRead != 4)
				return;

			bb.flip();
			final int length = bb.getInt();
			bb.clear();

			final ByteBuffer content = ByteBuffer.allocateDirect(length);
			if (socketChannel.read(content) != length)
				return;

			content.flip();
			final int id = content.getInt();

			read_handleRawPacket(id, content);

			content.clear();
		} catch (ClosedByInterruptException e) {
			// ignore because triggered in #close()
		} catch (ClosedChannelException e) {
			server.dispatchEvent(new ClosedSocketEvent(e, this));
			close();
		} catch (IOException e) {
			handleException(new P4JServerClientException(e));
		}
	}

	protected void read_handleRawPacket(int id, ByteBuffer content) {
		try {
			content = server.getCompression().decompress(content);
			content = server.getEncryption().decrypt(content);
			Object obj = server.getCodec().decode(content);

			C2SPacket packet = (C2SPacket) server.getPackets().packetInstance(id);

			packet.serverRead(this, obj);

			server.dispatchEvent(new C2SReadPacketEvent(this, packet, server.getPackets().getClass(id)));
		} catch (UnknownPacketException e) {
			server.dispatchEvent(new C2SReadPacketEvent(this, id, e));
			handleException(new P4JServerClientException(e));
		} catch (Exception e) {
			server.dispatchEvent(new C2SReadPacketEvent(this, id, e));
			handleException(new P4JServerClientException(e));
		}
	}

	/**
	 * Writes a packet from the server to the client.
	 * 
	 * @param S2CPacket the packet to write to the client
	 * @return If the packet was written successfully
	 */
	public synchronized boolean write(S2CPacket packet) {
		Objects.requireNonNull(packet);

		try {
			ByteBuffer content = server.getCodec().encode(packet.serverWrite(this));
			content = server.getEncryption().encrypt(content);
			content = server.getCompression().compress(content);

			final int id = server.getPackets().getId(packet.getClass());

			final ByteBuffer bb = ByteBuffer.allocateDirect(4 + 4 + content.capacity());
			bb.putInt(content.limit() + 4); // Add id length
			bb.putInt(id);
			bb.put(content);
			bb.flip();

			final int length = socketChannel.write(bb);

			server.dispatchEvent(new S2CWritePacketEvent(this, packet, id));
			return true;
		} catch (ClosedChannelException e) {
			server.dispatchEvent(new ClosedSocketEvent(e, this));
			server.dispatchEvent(new S2CWritePacketEvent(this, packet, e));
			return false;
		} catch (Exception e) {
			server.dispatchEvent(new S2CWritePacketEvent(this, packet, e));
			handleException(new P4JServerClientException(e));
			return false;
		}
	}

	/**
	 * Handles the given exception in this client instance.<br>
	 * It is strongly encouraged to override this method.
	 * 
	 * @param Exception the exception
	 */
	private void handleException(P4JServerClientException e) {
		if (exceptionConsumer != null) {
			exceptionConsumer.accept(e);
		}
	}

	/**
	 * Disconnects & closes the client socket<br>
	 * And dispatches a {@link ClosedSocketEvent}.
	 * 
	 * @see #close()
	 * @throws P4JClientException if the client socket is already closed or isn't started
	 */
	public void disconnect() {
		close();
		server.dispatchEvent(new ClosedSocketEvent(this));
	}

	/**
	 * Closes the client socket.<br>
	 * Doesn't dispatch a {@link ClosedSocketEvent}.
	 * 
	 * @see {@link #disconnect()}
	 * @throws P4JClientException if the client socket is already closed or isn't started
	 */
	@Override
	public void close() {
		if (serverClientStatus.equals(ServerClientStatus.CLOSED) || serverClientStatus.equals(ServerClientStatus.PRE))
			throw new P4JClientException("Cannot close unstarted client socket.");

		try {
			serverClientStatus = ServerClientStatus.CLOSING;
			socketChannel.close();
			serverClientStatus = ServerClientStatus.CLOSED;

			server.getClientManager().remove(this);
		} catch (Exception e) {
			handleException(new P4JServerClientException(e));
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public P4JServer getServer() {
		return server;
	}

	public UUID getUUID() {
		return uuid;
	}

	public ServerClientStatus getServerClientStatus() {
		return serverClientStatus;
	}

	public Consumer<P4JServerClientException> getExceptionConsumer() {
		return exceptionConsumer;
	}

	public void setExceptionConsumer(Consumer<P4JServerClientException> exceptionConsumer) {
		this.exceptionConsumer = exceptionConsumer;
	}

	@Override
	public String toString() {
		return getClass().getName() + "#" + hashCode() + "@{server=" + server + ", uuid=" + uuid + ", status=" + serverClientStatus + "}";
	}

}