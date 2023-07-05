package lu.pcy113.p4j.socket.client;

import java.net.InetSocketAddress;

public class ClientServer {

	private InetSocketAddress remoteInetSocketAddress;
	
	public ClientServer(InetSocketAddress inetSocketAddress) {
		this.remoteInetSocketAddress = inetSocketAddress;
	}
	
	public InetSocketAddress getRemoteInetSocketAddress() {
		return remoteInetSocketAddress;
	}
	
}