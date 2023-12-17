package lu.pcy113.p4j.events;

import java.util.List;

public interface EventQueueConsumer {

	EventQueueConsumer IGNORE = new EventQueueConsumer() {
		@Override
		public void handle(Event event) {
			// ignore the event
		}

		@Override
		public void addListener(Listener list) {
		}

		@Override
		public List<Listener> getListeners() {
			return null;
		}

		@Override
		public void removeListener(int i) {
		}

		@Override
		public void removeListener(Listener list) {
		}
	};

	void handle(Event event);

	List<Listener> getListeners();

	void addListener(Listener list);

	void removeListener(Listener list);

	void removeListener(int i);

}
