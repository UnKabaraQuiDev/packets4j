package lu.pcy113.p4j.socket.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.packets.PacketManager;
import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.socket.P4JInstance;
import lu.pcy113.p4j.socket.listeners.events.TransmitEvent;
import lu.pcy113.p4j.socket.listeners.events.handler.ClientEventHandler;
import lu.pcy113.p4j.socket.server.P4JServerException;
import lu.pcy113.p4j.util.ArrayUtils;
import lu.pcy113.p4j.compress.CompressionManager;

public class P4JClient extends Thread implements P4JInstance {

    private ClientStatus clientStatus = ClientStatus.PRE;

    private ClientEventHandler clientEventHandler = new ClientEventHandler();

    private CodecManager codec;
    private EncryptionManager encryption;
    private CompressionManager compression;
    private PacketManager packets = new PacketManager(this);

    private InetSocketAddress localInetSocketAddress;
    private SocketChannel clientSocketChannel;
    
    private ClientServer clientServer;

    public P4JClient(CodecManager cm, EncryptionManager em, CompressionManager com) {
        this.codec = cm;
        this.encryption = em;
        this.compression = com;
    }
    public void bind() throws IOException {bind(0);}
    public void bind(int port) throws IOException {
        clientSocketChannel = SocketChannel.open();
        clientSocketChannel.bind(new InetSocketAddress(port));
        clientStatus = ClientStatus.OPEN;
        
        this.localInetSocketAddress = new InetSocketAddress(clientSocketChannel.socket().getInetAddress(), clientSocketChannel.socket().getLocalPort());
        super.setName("P4JClient@"+localInetSocketAddress.getHostString()+":"+localInetSocketAddress.getPort());
    } 
    public void connect(InetAddress remote, int port) throws IOException {
        clientSocketChannel.connect(new InetSocketAddress(remote, port));
        clientSocketChannel.configureBlocking(true);
        clientStatus = ClientStatus.LISTENING;
        
        clientServer = new ClientServer(new InetSocketAddress(clientSocketChannel.socket().getInetAddress(), clientSocketChannel.socket().getPort()));
        
        super.start();
    }

    @Override
    public void run() {
        while(clientStatus.equals(ClientStatus.LISTENING)) {
            read();
        }
        clientStatus = ClientStatus.CLOSED;
    }

    public void read() {
    	try {
	        ByteBuffer bb = ByteBuffer.allocate(4);
	        if(clientSocketChannel.read(bb) != 4)
	            return;
	
	        int length = bb.getInt();
	        bb.clear();
	        
	        ByteBuffer content = ByteBuffer.allocate(length);
	        if(clientSocketChannel.read(content) != length)
	            return;
	        
	        int id = content.getInt();
	
	        read_handleRawPacket(id, content);
	    }catch(ClosedByInterruptException e) {
	    	// ignore because triggered in #close()
	    }catch(IOException e) {
			handleException("read", e);
		}
    }
    protected void read_handleRawPacket(int id, ByteBuffer content) {
    	try {
            content = compression.decompress(content);
	        content = encryption.decrypt(content);
	        Object obj = codec.decode(content);
	        
	        S2CPacket packet = (S2CPacket) packets.packetInstance(id);
	        packet.clientRead(this, obj);

            clientEventHandler.appendReceive(new ReceiveEvent<P4JClient>(this, Packet));
	    }catch(Exception e) {
			handleException("read_handleRawPacket", e);
		}
    }
    
    public boolean write(C2SPacket packet) {
    	try {
	        Object obj = packet.clientWrite(this);

            TransmitEvent<P4JInstance> te = new TransmitEvent<P4JClient>(this, packet);
            clientEventHandler.handleEvent(te);
            if(te.isCancelled()) {
                logger.info("Packet "+packet+", cancelled by: "+te.getCallerClasses().entrySet().stream().sorted(Maps.reverseOrder()).anyMatch((s) -> s.getValue() == true)).get();
                return;
            }

	        ByteBuffer content = codec.encode(obj);
            content = compression.compress(content);
	        content = encryption.encrypt(content);
	
	        ByteBuffer bb = ByteBuffer.allocate(4+4+content.capacity());
	        bb.putInt(content.limit() + 4); // Add id length
	        bb.putInt(packets.getId(packet.getClass()));
	        bb.put(content);
	        bb.flip();
	        
	        System.out.println(ArrayUtils.byteArrayToHexString(bb.array()));
	        
	        clientSocketChannel.write(bb);
	        
	        return true;
	    }catch(Exception e) {
			handleException("write", e);
			return false;
		}
    }
    
    public void close() {
        if(clientStatus.equals(ClientStatus.CLOSED) || clientStatus.equals(ClientStatus.PRE))
            throw new P4JServerException("Cannot close not started client socket.");
        //kickClients("Server Closed", true);
        try {
        	clientStatus = ClientStatus.CLOSING;
        	this.interrupt();
	        clientSocketChannel.close();
	        clientStatus = ClientStatus.CLOSED;
        }catch(IOException e) {
        	handleException("close", e);
        }
    }
    
    protected void handleException(String msg, Exception e) {
    	System.err.println(getClass().getName()+"/"+localInetSocketAddress+"> "+msg+" ::");
    	e.printStackTrace(System.err);
    }
    
    public ClientStatus getClientStatus() {return clientStatus;}
    public InetSocketAddress getLocalInetSocketAddress() {return localInetSocketAddress;}
    public ClientServer getClientServer() {return clientServer;}

    public CodecManager getCodec() {return codec;}
    public EncryptionManager getEncryption() {return encryption;}
    public CompressionManager getCompression() {return compression;}
    public PacketManager getPackets() {return packets;}
    public void setCodec(CodecManager codec) {this.codec = codec;}
    public void setEncryption(EncryptionManager encryption) {this.encryption = encryption;}
    public void setCompression(CompressionManager compression) {this.compression = compression;}
    public void setPackets(PacketManager packets) {this.packets = packets;}
    
}