package lu.pcy113.p4j.socket.server;

import java.net.ProtocolFamily;
import java.nio.channels.ServerSocketChannel;

import javax.net.ServerSocketFactory;

import src.lu.pcy113.p4j.codec.CodecManager;

public class P4JServer extends Thread {

    private CodecManager codec;
    private EncryptionManager encryption;

    private ServerStatus serverStatus;

    private ServerSocketChannel serverSocketChannel;
    private Selector serverSocketSelector;
    
    public P4JServer(CodecManager cm, EncryptionManager em) {
        this.codec = cm;
        this.encryption = em;
    }
    public bind(InetAddress ia, int port) {
        serverSocketSelector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(ia, port);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(serverSocketSelector, SelectionKey.OP_ACCEPT);
    }

    public void run() {
        while(!serverStatus.equals(ServerStatus.ACCEPTING)) {
            selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        // Accept a new client connection
                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);

                        // Register the client socket channel with the selector for reading
                        clientChannel.register(selector, SelectionKey.OP_READ);

                        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // Read data from a client socket channel
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        int bytesRead = clientChannel.read(buffer);

                        if (bytesRead == -1) {
                            // Connection closed by client
                            clientChannel.close();
                            key.cancel();
                            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
                        } else if (bytesRead > 0) {
                            // Process the received data
                            buffer.flip();
                            byte[] data = new byte[buffer.limit()];
                            buffer.get(data);
                            String message = new String(data);
                            System.out.println("Received from client: " + message);

                            // TODO: Handle the received data as per your application's logic
                        }
                    }

                    keyIterator.remove();
        }
    }

    public void kickClients(String msg, boolean force) {

    }

    public void setAccepting() {
        if(serverStatus.equals(ServerStatus.CLOSED))
            throw new P4JServerException("Cannot set closed server socket in client accept mode.");
        serverStatus = ServerStatus.ACCEPTING;
    }
    public void close() {
        if(serverStatus.equals(ServerStatus.CLOSED) || serverStatus.equals(ServerStatus.PRE))
            throw new P4JServerException("Cannot close not started server socket.");
        kickClients("Server Closed", true);
        serverSocketChannel.close();
        serverStatus = ServerStatus.CLOSED;
    }
    public void setRefusing() {
        if(serverStatus.equals(ServerStatus.CLOSED))
            throw new P4JServerException("Cannot set closed server socket in client refuse mode.");
        if(!super.isAlive())
            super.start();
    }
    public ServerStatus getServerStatus() {return serverStatus;}

}