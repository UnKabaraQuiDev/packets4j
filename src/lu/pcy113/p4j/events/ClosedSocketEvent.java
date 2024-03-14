package lu.pcy113.p4j.events;

import java.nio.channels.ClosedChannelException;

import lu.pcy113.p4j.socket.P4JClientInstance;

public class ClosedSocketEvent implements Event {

	private ClosedChannelException exception;
	private P4JClientInstance client;

	public ClosedSocketEvent(ClosedChannelException e, P4JClientInstance op) {
		this.exception = e;
		this.client = op;
	}

	public boolean isFail() {return exception != null;}
	public ClosedChannelException getException() {return exception;}
	public P4JClientInstance getClient() {return client;}
	public void setException(ClosedChannelException exception) {this.exception = exception;}
	public void setClient(P4JClientInstance operator) {this.client = operator;}

}
