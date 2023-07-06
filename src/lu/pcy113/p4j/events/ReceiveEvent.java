package lu.pcy113.p4j.events;

import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.util.Cancellable;

public class ReceiveEvent<T> implements Event, Cancellable {
    
    private T receiver;
    private Packet packet;
    private boolean cancelled = false;

    public ReceiveEvent(T rec, Packet p) {
        this.receiver = rec;
        this.packet = p;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    public Packet getPacket() {
        return packet;
    }
    public T getReceiver() {
        return receiver;
    }
    public void setPacket(Packet packet) {
        this.packet = packet;
    }
    public void setReceiver(T receiver) {
        this.receiver = receiver;
    }
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
