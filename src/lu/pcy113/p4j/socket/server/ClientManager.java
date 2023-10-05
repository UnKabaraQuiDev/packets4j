package lu.pcy113.p4j.socket.server;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import lu.pcy113.p4j.socket.events.ClientInstanceConnectedEvent;

public class ClientManager {
	
	private P4JServer server;
	
	private HashMap<SocketChannel, ServerClient> clients = new HashMap<>();
	
	public ClientManager(P4JServer server) {
		this.server = server;
	}
	
	public void register(SocketChannel sc) {
		ServerClient sclient = new ServerClient(sc, this.server);
		registerClient(sclient);
		server.listenersConnected.handle(new ClientInstanceConnectedEvent(sclient, server));
	}

	public ServerClient get(SocketChannel clientChannel) {
		return clients.get(clientChannel);
	}
	public ServerClient get(UUID uuid) {
		return clients.values().parallelStream().filter(sc -> sc.getUUID().equals(uuid)).findFirst().orElse(null);
	}
	
	public void registerClient(ServerClient sclient) {
		clients.put(sclient.getSocketChannel(), sclient);
	}
	
	public Set<SocketChannel> allSockets() {
		return clients.keySet();
	}
	public Collection<ServerClient> allClients() {
		return clients.values();
	}
	public Set<Entry<SocketChannel, ServerClient>> all() {
		return clients.entrySet();
	}
	
}
