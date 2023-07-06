package lu.pcy113.p4j.socket.listeners;

import lu.pcy113.p4j.socket.client.P4JClient;

public interface ClientListener extends Listener {

    void receive(ReceiveEvent<P4JClient> event);
    void transmit(TransmitEvent<P4JClient> event);

}
