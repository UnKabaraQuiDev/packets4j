package lu.pcy113.p4j.events.client;

import lu.pcy113.p4j.events.P4JEvent;
import lu.pcy113.p4j.socket.P4JClientInstance;
import lu.pcy113.p4j.socket.P4JServerInstance;

public class ClientConnectedEvent implements P4JEvent {

	private P4JClientInstance client;
	private P4JServerInstance server;

	public ClientConnectedEvent(P4JClientInstance client, P4JServerInstance clientServer) {
		this.client = client;
		this.server = clientServer;
	}

	public P4JClientInstance getClient() {
		return client;
	}

	public P4JServerInstance getServer() {
		return server;
	}

	public void setClient(P4JClientInstance client) {
		this.client = client;
	}

	public void setServer(P4JServerInstance server) {
		this.server = server;
	}

}
