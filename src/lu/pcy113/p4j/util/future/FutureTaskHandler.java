package lu.pcy113.p4j.util.future;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class FutureTaskHandler extends Thread {
    
	private boolean running;
	private Object lock = new Object();
    private LinkedHashMap<Object, FutureTask<?, ?>> queue = new LinkedHashMap<>();
    
    public FutureTaskHandler() {
    	running = true;
    	super.setName(getClass().getName()+"#"+hashCode());
    	super.setDaemon(true);
    	super.start();
	}
    
    @Override
    public void run() {
    	try {
    		synchronized (lock) {
		        while(running) {
		            exec();
		            lock.wait();
		        }
    		}
    	}catch(InterruptedException e) {
    		// probably stopped
    	}
    }
    
    private void exec() {
    	Iterator<Entry<Object, FutureTask<?, ?>>> e = queue.entrySet().iterator();
        while(e.hasNext()) {
            Entry<Object, FutureTask<?, ?>> ev = e.next();
            handleFutureTask(ev.getValue(), ev.getKey());
            e.remove();
        }
    }
    
    protected void handleFutureTask(FutureTask ft, Object o) {
    	ft.complete(o);
    }
    
    public <I> void append(FutureTask<I, ?> ft, I in) {
    	synchronized (lock) {
	        queue.put(in, ft);
	        if(super.isAlive())
	        	lock.notify();
    	}
    }
    
    public void stopHandler(boolean force) {
    	synchronized (lock) {
	        running = false;
	        lock.notify();
    	}
    	if(force) {
    		exec();
    	}
    }
    public void startHandler() {
        running = true;
        if(!super.isAlive())
            super.start();
    } 

}
