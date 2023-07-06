package lu.pcy113.p4j.socket.listeners;

import lu.pcy113.p4j.socket.client.P4JClient;

public interface ServerListener extends Listener {

    void receive(ReceiveEvent<P4JServer> event);
    void transmit(TransmitEvent<P4JServer> event);

}
