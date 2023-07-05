package lu.pcy113.p4j.sockets.client;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.SocketFactory;

import src.lu.pcy113.p4j.codec.CodecManager;
import src.lu.pcy113.p4j.crypto.EncryptionManager;
import src.lu.pcy113.p4j.packets.PacketManager;

public class P4JClient extends Thread {

    private ClientStatus clientStatus = ClientStatus.PRE;

    private CodecManager codec;
    private EncryptionManager encryption;
    private PacketManager packets = new PacketManager(this);

    private SocketChannel clientSocketChannel;

    public P4JClient(CodecManager cm, EncryptionManager em) {
        this.codec = cm;
        this.encryption = em;
    }
    public void bind() {
        clientSocketChannel = SocketChannel.open();
        clientStatus = ClientStatus.OPEN();
    } 
    public void connect(InetAddress remote, int port) {
        clientSocketChannel.connect(new InetSocketAddress(remote, port));
        clientSocketChannel.configureBlocking(true);
        clientStatus = clientStatus.CONNECTED;
    }

    @Override
    public void run() {
        while(clientStatus.equals(ClientStatus.LISTENING)) {
            read();
        }
    }

    public void read() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        if(clientSocketChannel.read(bb) != 4)
            continue;

        int length = bb.getInt();
        ByteBuffer content = ByteBuffer.allocate(length);
        if(clientSocketChannel.read(content) != length)
            continue;
        int id = content.getInt();

        read_handleRawPacket(id, content);
    }
    protected void read_handleRawPacket(int id, ByteBuffer content) {
        content = encryption.decrypt(content);
        Object obj = codec.decode(content);
        
        S2CPacket packet = packets.packetInstance(id);
        packet.clientRead(this, obj);
    }
    
    public void write(C2SPacket packet) {
        Object obj = packet.clientWrite(this);
        ByteBuffer content = codec.encode(obj);
        content = encryption.encrypt(content);

        ByteBuffer bb = ByteBuffer.allocate(4+4+content.capacity());
        bb.putInt(packet.id());
        bb.putInt(content.capacity());
        bb.put(content);
        clientSocketChannel.write(bb);
    }

}