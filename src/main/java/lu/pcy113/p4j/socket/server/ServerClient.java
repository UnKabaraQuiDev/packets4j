package lu.pcy113.p4j.socket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import lu.pcy113.p4j.events.C2SReadPacketEvent;
import lu.pcy113.p4j.events.ClosedSocketEvent;
import lu.pcy113.p4j.events.S2CWritePacketEvent;
import lu.pcy113.p4j.packets.UnknownPacketException;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.client.P4JClientException;

/**
 * Represents a P4JServer's client. This is a wrapper around a {@link SocketChannel} that represents a client connected to the server, 
 * on the server-side.
 */
public class ServerClient implements P4JClientInstance {

	private ServerClientStatus serverClientStatus = ServerClientStatus.PRE;

	private UUID uuid;
	private P4JServer server;

	private SocketChannel socketChannel;

	public ServerClient(SocketChannel sc, P4JServer server) {
		this.socketChannel = sc;
		this.server = server;

		this.uuid = UUID.randomUUID();

		this.serverClientStatus = ServerClientStatus.LISTENING;
	}

	public void read() {
		try {
			final ByteBuffer bb = ByteBuffer.allocateDirect(4);
			final int bytesRead = socketChannel.read(bb);
			if(bytesRead == -1) {
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
			handleException("read", e);
		}
	}

	protected void read_handleRawPacket(int id, ByteBuffer content) {
		try {
			content = server.getCompression().decompress(content);
			content = server.getEncryption().decrypt(content);
			Object obj = server.getCodec().decode(content);

			C2SPacket packet = (C2SPacket) server.getPackets().packetInstance(id);

			server.dispatchEvent(new C2SReadPacketEvent(this, packet, server.getPackets().getClass(id)));

			packet.serverRead(this, obj);
		} catch (UnknownPacketException e) {
			server.dispatchEvent(new C2SReadPacketEvent(this, id, e));
		} catch (Exception e) {
			server.dispatchEvent(new C2SReadPacketEvent(this, id, e));
			handleException("read_handleRawPacket", e);
		}
	}

	/**
	 * Writes a packet from the server to the client.
	 * 
	 * @param S2CPacket the packet to write to the client
	 * @return If the packet was written successfully
	 */
	public synchronized boolean write(S2CPacket packet) {
		try {
			ByteBuffer content = server.getCodec().encode(packet.serverWrite(this));
			content = server.getEncryption().encrypt(content);
			content = server.getCompression().compress(content);

			ByteBuffer bb = ByteBuffer.allocateDirect(4 + 4 + content.capacity());
			bb.putInt(content.limit() + 4); // Add id length
			bb.putInt(server.getPackets().getId(packet.getClass()));
			bb.put(content);
			bb.flip();

			int length = socketChannel.write(bb);

			server.dispatchEvent(new S2CWritePacketEvent(this, packet));
			return true;
		} catch (ClosedChannelException e) {
			server.dispatchEvent(new ClosedSocketEvent(e, this));
			server.dispatchEvent(new S2CWritePacketEvent(this, packet, e));
			handleException("write", e);
			return false;
		} catch (Exception e) {
			server.dispatchEvent(new S2CWritePacketEvent(this, packet, e));
			handleException("write", e);
			return false;
		}
	}

	/**
	 * Handles the given exception in this client instance.<br>
	 * It is strongly encouraged to override this method.
	 * 
	 * @param String the message (the context) ("read", "read_handleRawPacket", "write", "close")
	 * @param Exception the exception
	 */
	protected void handleException(String msg, Exception e) {
		System.err.println(getClass().getName() + "/" + uuid + "> " + msg + " ::");
		e.printStackTrace(System.err);
	}

	/**
	 * Closes the client socket.
	 * 
	 * @throws P4JClientException if the client socket is already closed or isn't started
	 */
	public void close() {
		if (serverClientStatus.equals(ServerClientStatus.CLOSED) || serverClientStatus.equals(ServerClientStatus.PRE))
			throw new P4JClientException("Cannot close unstarted client socket.");

		try {
			serverClientStatus = ServerClientStatus.CLOSING;
			socketChannel.close();
			serverClientStatus = ServerClientStatus.CLOSED;
		} catch (IOException e) {
			handleException("close", e);
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

}