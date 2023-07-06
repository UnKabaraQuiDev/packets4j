package lu.pcy113.p4j.events.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lu.pcy113.p4j.events.Event;
import lu.pcy113.p4j.events.ReceiveEvent;
import lu.pcy113.p4j.events.TransmitEvent;
import lu.pcy113.p4j.events.listener.Listener;

public class EventHandler extends Thread {
    
    private boolean running, async;
    
    private List<Listener> listeners = new ArrayList<>();
    private List<Event> events = new ArrayList<>();

    public EventHandler() {
        this(true);
    }
    public EventHandler(boolean async) {
        this.async = async;
        this.running = true;
        
        super.setName(getClass().getName()+"#"+hashCode());
    	super.setDaemon(true);
        if(async)
            super.start();
    }

    @Override
    public void run() {
    	try {
    		synchronized(this) {
		        while(running) {
		            iterate();
	            	wait();
		        }
    		}
    	}catch(InterruptedException e) {
    		// probably stopped
    	}
    }

    protected void iterate() {
    	Iterator<Event> e = events.iterator();
        while(e.hasNext()) {
            Event ev = e.next();
            handleEvent(ev);
            e.remove();
        }
    }
    public void handleEvent(Event e) {
        for(Listener l : listeners) {
            handleEvent(l, e);
        }
    }
    public void handleEvent(Listener l, Event e) {
    	if(e instanceof ReceiveEvent) {
    		l.receive((ReceiveEvent) e);
    	}else if(e instanceof TransmitEvent) {
    		l.transmit((TransmitEvent) e);
    	}
    }
    
    public void register(Listener l) {
    	listeners.add(l);
    }
    public boolean unregister(Listener l) {
    	return listeners.remove(l);
    }
    public Listener unregister(int i) {
    	return listeners.remove(i);
    }
    
    public void appendEvent(Event e) {
    	if(async) {
	    	synchronized(this) {
		        events.add(e);
		    	notify();
	    	}
    	}else {
    		handleEvent(e);
    	}
    }
    
    public void stopHandler() {
        running = false;
        notify();
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
