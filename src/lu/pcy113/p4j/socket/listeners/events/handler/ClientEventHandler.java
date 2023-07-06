package lu.pcy113.p4j.socket.listeners.events.handler;

import lu.pcy113.p4j.socket.listeners.ClientListener;
import lu.pcy113.p4j.socket.listeners.events.ReceiveEvent;
import lu.pcy113.p4j.socket.listeners.events.TransmitEvent;

public class ClientEventHandler extends EventHandler {

    public ClientEventHandler() {this(true);}
    public ClientEventHandler(boolean async) {
        super(true);
    }

    public void handleEvent(Listener l, Event e) {
        if(l instanceof ClientListener) {
            if(e instanceof ReceiveEvent) {
                ((ClientListener) l).receive((ReceiveEvent<P4JClient>) e);
            }
            if(e instanceof TransmitEvent) {
                ((ClientListener) l).transmit((TransmitEvent<P4JClient>) e);
            }
        }
        if(l instanceof ServerListener) {
            if(e instanceof ReceiveEvent) {
                ((ServerListener) l).receive((ReceiveEvent<P4JServer>) e);
            }
            if(e instanceof TransmitEvent) {
                ((ServerListener) l).transmit((TransmitEvent<P4JServer>) e);
            }
        }
    }

}