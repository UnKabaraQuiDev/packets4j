package lu.pcy113.p4j.events;

import java.util.ArrayList;

public class Listeners extends ArrayList<Listener> {
	
	public void handle(Event e) {
		for(Listener l : this) {
			l.handle(e);
		}
	}
	
}
