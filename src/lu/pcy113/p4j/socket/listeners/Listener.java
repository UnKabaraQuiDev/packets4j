package lu.pcy113.p4j.socket.listeners;

public interface Listener {

    void receive(ReceiveEvent<P4JInstance> event);
    void transmit(TransmitEvent<P4JInstance> event);

}