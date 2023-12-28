package lu.pcy113.p4j.socket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import lu.pcy113.p4j.events.ClientReadPacketEvent;
import lu.pcy113.p4j.events.ClientWritePacketEvent;
import lu.pcy113.p4j.events.ClosedChannelEvent;
import lu.pcy113.p4j.packets.UnknownPacketException;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.client.P4JClientException;

public class ServerClient implements P4JClientInstance {

	private ServerClientstatus serverClientStatus = ServerClientstatus.PRE;

	private UUID uuid;
	private P4JServer server;

	private SocketChannel socketChannel;

	public ServerClient(SocketChannel sc, P4JServer server) {
		this.socketChannel = sc;
		this.server = server;

		this.uuid = UUID.randomUUID();

		this.serverClientStatus = ServerClientstatus.LISTENING;
	}

	public void read() {
		try {
			ByteBuffer bb = ByteBuffer.allocateDirect(4);
			if (socketChannel.read(bb) != 4)
				return;

			bb.flip();
			int length = bb.getInt();
			bb.clear();

			ByteBuffer content = ByteBuffer.allocateDirect(length);
			if (socketChannel.read(content) != length)
				return;

			content.flip();
			int id = content.getInt();

			// System.out.println("serverclient#read:
			// "+ArrayUtils.byteBufferToHexString(content));

			read_handleRawPacket(id, content);

			content.clear();
		} catch (ClosedByInterruptException e) {
			// ignore because triggered in #close()
		} catch (ClosedChannelException e) {
			// ignore
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

			server.events.handle(new ClientReadPacketEvent(this, packet, server.getPackets().getClass(id)));

			packet.serverRead(this, obj);
		} catch (UnknownPacketException e) {
			server.events.handle(new ClientReadPacketEvent(this, id, e));
		} catch (Exception e) {
			server.events.handle(new ClientReadPacketEvent(this, id, e));
			handleException("read_handleRawPacket", e);
		}
	}

	public boolean write(S2CPacket packet) {
		try {
			ByteBuffer content = server.getCodec().encode(packet.serverWrite(this));
			// System.err.println("server sent: " +
			// ArrayUtils.byteBufferToHexString(content));
			content = server.getEncryption().encrypt(content);
			content = server.getCompression().compress(content);

			ByteBuffer bb = ByteBuffer.allocateDirect(4 + 4 + content.capacity());
			bb.putInt(content.limit() + 4); // Add id length
			bb.putInt(server.getPackets().getId(packet.getClass()));
			bb.put(content);
			bb.flip();

			// System.out.println("serverclient#write:
			// "+ArrayUtils.byteBufferToHexString(bb));

			socketChannel.write(bb);

			server.events.handle(new ClientWritePacketEvent(this, packet));
			// socketChannel.socket().getOutputStream().flush();
			return true;
		} catch (Exception e) {
			server.events.handle(new ClientWritePacketEvent(this, packet, e));
			handleException("write", e);
			return false;
		}
	}

	protected void handleException(String msg, Exception e) {
		System.err.println(getClass().getName() + "/" + uuid + "> " + msg + " ::");
		e.printStackTrace(System.err);
	}

	public void close() {
		if (serverClientStatus.equals(ServerClientstatus.CLOSED) || serverClientStatus.equals(ServerClientstatus.PRE))
			throw new P4JClientException("Cannot close unstarted client socket.");

		try {
			serverClientStatus = ServerClientstatus.CLOSING;
			socketChannel.close();
			serverClientStatus = ServerClientstatus.CLOSED;

			server.events.handle(new ClosedChannelEvent(null, this));
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