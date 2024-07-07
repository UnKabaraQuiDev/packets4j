package lu.pcy113.p4j.events;

import java.nio.channels.ClosedChannelException;

import lu.pcy113.p4j.socket.P4JClientInstance;

public class ClosedSocketEvent implements P4JEvent {

	private Exception exception;
	private P4JClientInstance client;

	public ClosedSocketEvent(Exception e, P4JClientInstance op) {
		this.exception = e;
		this.client = op;
	}

	public ClosedSocketEvent(P4JClientInstance client) {
		this.client = client;
	}

	public boolean isFail() {
		return exception != null;
	}

	public Exception getException() {
		return exception;
	}

	public P4JClientInstance getClient() {
		return client;
	}

	public void setException(ClosedChannelException exception) {
		this.exception = exception;
	}

	public void setClient(P4JClientInstance operator) {
		this.client = operator;
	}

}
