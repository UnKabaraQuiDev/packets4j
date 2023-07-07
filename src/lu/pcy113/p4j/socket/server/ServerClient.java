package lu.pcy113.p4j.socket.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.UUID;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.util.ArrayUtils;

public class ServerClient {

	private UUID uuid;
	private P4JServer server;
	
    private SocketChannel socketChannel;

    public ServerClient(SocketChannel sc, P4JServer server) {
        this.socketChannel = sc;
        this.server = server;
        
        this.uuid = UUID.randomUUID();
    }

    public void read() {
    	try {
	        ByteBuffer bb = ByteBuffer.allocateDirect(4);
	        if(socketChannel.read(bb) != 4)
	            return;
	        
	        bb.flip();
	        int length = bb.getInt();
	        bb.clear();
	        
	        ByteBuffer content = ByteBuffer.allocateDirect(length);
	        if(socketChannel.read(content) != length)
	            return;
	        
	        content.flip();
	        int id = content.getInt();
	        
	        //System.out.println("serverclient#read: "+ArrayUtils.byteBufferToHexString(content));
	
	        read_handleRawPacket(id, content);
    	}catch(IOException e) {
    		handleException("read", e);
    	}
    }
    protected void read_handleRawPacket(int id, ByteBuffer content) {
    	try {
			content = server.getCompression().decompress(content);
	        content = server.getEncryption().decrypt(content);
	        Object obj = server.getCodec().decode(content);
	        
	        C2SPacket packet = (C2SPacket) server.getPackets().packetInstance(id);
	        packet.serverRead(this, obj);
    	}catch(Exception e) {
    		handleException("read_handleRawPacket", e);
    	}
    }
    public boolean write(S2CPacket packet) {
    	try {
	    	ByteBuffer content = server.getCodec().encode(packet.serverWrite(this));
	    	content = server.getEncryption().encrypt(content);
	        content = server.getCompression().compress(content);
	        
	        ByteBuffer bb = ByteBuffer.allocateDirect(4+4+content.capacity());
	        bb.putInt(content.limit() + 4); // Add id length
	        bb.putInt(server.getPackets().getId(packet.getClass()));
	        bb.put(content);
	        bb.flip();
	        
	        //System.out.println("serverclient#write: "+ArrayUtils.byteBufferToHexString(bb));
	        
			socketChannel.write(bb);
			//socketChannel.socket().getOutputStream().flush();
	        return true;
    	}catch(Exception e) {
    		handleException("write", e);
    		return false;
    	}
    }
    
    protected void handleException(String msg, Exception e) {
    	System.err.println(getClass().getName()+"/"+uuid+"> "+msg+" ::");
    	e.printStackTrace(System.err);
    }

    public SocketChannel getSocketChannel() {return socketChannel;}
    public UUID getUUID() {return uuid;}

}