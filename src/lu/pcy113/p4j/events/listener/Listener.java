package lu.pcy113.p4j.events.listener;

import lu.pcy113.p4j.events.ReceiveEvent;
import lu.pcy113.p4j.events.TransmitEvent;

public interface Listener<T> {

    void receive(ReceiveEvent<T> event);
    void transmit(TransmitEvent<T> event);

}