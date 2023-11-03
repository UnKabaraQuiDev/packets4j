package lu.pcy113.p4j.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AsyncEventQueueConsumer extends Thread implements EventQueueConsumer {
	
	private Object lock;
	private Queue<Event> events;
	private List<Listener> listeners = new ArrayList<Listener>();
	
	public AsyncEventQueueConsumer() {
		this(false);
	}
	public AsyncEventQueueConsumer(boolean daemon) {
		super("AsyncEventQueueConsumer");
		super.setDaemon(daemon);
		
		lock = new Object();
		events = new ConcurrentLinkedQueue<>();
		
		super.start();
	}
	
	@Override
	public void handle(Event event) {
        synchronized (lock) {
            events.add(event);
            lock.notify(); // Notify the thread that an event is appended
        }
    }

    @Override
    public void run() {
        while (true) {
            Event currentEvent;
            synchronized (lock) {
                while (events.isEmpty()) {
                    try {
                        lock.wait(); // Wait if the queue is empty
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                currentEvent = events.poll(); // Get the next event to process
            }

            processEvent(currentEvent);
        }
    }

	private void processEvent(Event e) {
		for(Listener l : listeners) {
			l.handle(e);
		}
	}

	@Override
	public List<Listener> getListeners() {
		return listeners;
	}
	@Override
	public void addListener(Listener list) {
		listeners.add(list);
	}
	@Override
	public void removeListener(Listener list) {
		listeners.remove(list);
	}
	@Override
	public void removeListener(int i) {
		listeners.remove(i);
	}

}
