package lu.pcy113.p4j.events;

import java.util.ArrayList;
import java.util.List;

public class SyncEventQueueConsumer implements EventQueueConsumer {

	private List<Listener> listeners = new ArrayList<Listener>();

	@Override
	public void handle(Event event) {
		processEvent(event);
	}

	private void processEvent(Event e) {
		for (Listener l : listeners) {
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
