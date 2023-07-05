package lu.pcy113.p4j.socket.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerClient {

    private SocketChannel socketChannel;
    private P4JServer server;

    public ServerClient(SocketChannel sc, P4JServer server) {
        this.socketChannel = sc;
        this.server = server;
    }

    public void read() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        if(socketChannel.read(bb) != 4)
            return;
        
        int length = bb.getInt();
        ByteBuffer content = ByteBuffer.allocate(length);
        if(socketChannel.read(content, length) != length)
            return;
        bb.clear();
        int id = content.getInt();

        read_handleRawPacket(id, content);
    }
    protected void read_handleRawPacket(int id, ByteBuffer content) {
        content = server.getEncryption().decrypt(content);
        Object obj = server.getCodec().decode(content);
        
        C2SPacket packet = server.getPackets().packetInstance(id);
        packet.serverRead(this, obj);
    }
    public void write(S2CPacket packet) {
        socketChannel.write(packet.serverWrite(this));
    }

    public SocketChannel getSocketChannel() {return socketChannel;}

}