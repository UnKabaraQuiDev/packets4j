package lu.pcy113.p4j.socket.listeners.events;

import java.util.Map;
import lu.pcy113.p4j.socket.P4JInstance;
import lu.pcy113.p4j.packets.Packet;
import lu.pcy113.p4j.util.Cancellable;
import java.util.HashMap;

public class TransmitEvent<T extends P4JInstance> implements Event, Cancellable {
    
    private T receiver;
    private Packet packet;
    private boolean cancelled = false;
    private Map<Object, Boolean> callerClasses = new HashMap<>();

    public TransmitEvent(T rec, Packet p) {
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
    public P4JInstance getReceiver() {
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
        callerClasses.put(Reflection.getCallerClass(1), cancelled);
        this.cancelled = cancelled;
    }
    @Override
    public Map<Object, Boolean> getCallerClasses() {
        return callerClasses;
    }

}
