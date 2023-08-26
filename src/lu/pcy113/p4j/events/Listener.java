package lu.pcy113.p4j.events;

@FunctionalInterface
public interface Listener {
	
	void handle(Event event);
	
}
