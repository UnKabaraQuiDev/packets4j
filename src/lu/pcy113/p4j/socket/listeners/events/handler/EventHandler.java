package lu.pcy113.p4j.socket.listeners.events.handler;

import java.util.Iterator;
import java.util.List;

import lu.pcy113.p4j.socket.listeners.Listener;

public abstract class EventHandler extends Thread {
    
    private boolean running, async;

    private List<Listener> listeners = new ArrayList<>();
    private List<Event> events = new ArrayList<>();

    public EventHandler() {
        this(true);
    }
    public EventHandler(boolean async) {
        this.async = async;
        this.running = true;
        if(async)
            super.start();
    }

    @Override
    public void run() {
        while(running) {
            Iterator<Event> e = events.iterator();
            while(e.hasNext()) {
                Event ev = e.next();
                handleEvent(ev);
                e.remove();
            }
            if(async)
                super.wait();
        }
    }

    public void handleEvent(Event e) {
        for(Listener l : listeners) {
            handleEvent(l, e);
        }
    }
    public abstract void handleEvent(Listener l, Event e);

    public void appendEvent(Event e) {
        events.add(e);
        super.notify();
    }
    
    public void stopHandler() {
        running = false;
        super.notify();
    }
    public void startHandler() {
        running = true;
        if(async)
            if(!super.isAlive())
                super.start();
        else
            run();
    } 

}
