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
		this(true);
	}

	public AsyncEventQueueConsumer(boolean daemon) {
		super("AsyncEventQueueConsumer");
		super.setDaemon(daemon);

		this.lock = new Object();
		this.events = new ConcurrentLinkedQueue<>();

		super.start();
	}

	@Override
	public void handle(Event event) {
		synchronized (this.lock) {
			this.events.add(event);
			this.lock.notify(); // Notify the thread that an event is appended
		}
	}

	@Override
	public void run() {
		while (true) {
			Event currentEvent;
			synchronized (this.lock) {
				while (this.events.isEmpty()) {
					try {
						this.lock.wait(); // Wait if the queue is empty
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
				}
				currentEvent = this.events.poll(); // Get the next event to process
			}

			this.processEvent(currentEvent);
		}
	}

	private void processEvent(Event e) {
		for (Listener l : this.listeners) {
			l.handle(e);
		}
	}

	@Override
	public List<Listener> getListeners() {
		return this.listeners;
	}

	@Override
	public void addListener(Listener list) {
		this.listeners.add(list);
	}

	@Override
	public void removeListener(Listener list) {
		this.listeners.remove(list);
	}

	@Override
	public void removeListener(int i) {
		this.listeners.remove(i);
	}

}
