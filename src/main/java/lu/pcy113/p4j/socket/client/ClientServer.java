package lu.pcy113.p4j.socket.client;

import java.net.InetSocketAddress;

import lu.pcy113.p4j.socket.P4JServerInstance;

/**
 * Represents the server on the client-side.
 */
public class ClientServer implements P4JServerInstance {

	private InetSocketAddress remoteInetSocketAddress;

	public ClientServer(InetSocketAddress inetSocketAddress) {
		this.remoteInetSocketAddress = inetSocketAddress;
	}

	public InetSocketAddress getRemoteInetSocketAddress() {
		return remoteInetSocketAddress;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "#" + hashCode() + "@{" + remoteInetSocketAddress + "}";
	}

}