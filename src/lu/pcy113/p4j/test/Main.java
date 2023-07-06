package lu.pcy113.p4j.test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.compress.CompressionManager;
import lu.pcy113.p4j.crypto.EncryptionManager;
import lu.pcy113.p4j.events.ReceiveEvent;
import lu.pcy113.p4j.events.TransmitEvent;
import lu.pcy113.p4j.events.listener.Listener;
import lu.pcy113.p4j.socket.client.P4JClient;
import lu.pcy113.p4j.socket.server.P4JServer;
import lu.pcy113.p4j.socket.server.ServerClient;

public class Main {
    
    public static void main(String args[]) throws Exception {
    	
    	/*FutureTask<Integer, Void> ft1 = new FutureTask<Integer, Boolean>() {
	    	@Override
	    	public Boolean complete(Integer in) {
	    		return in != 0;
	    	}
    	}.<String>then((b) -> b ? "succeed" : "not").<Void>then((s) -> {System.out.println(s); return null;});
    	ft1.complete(012);
    	
    	Thread.sleep(2000);
    	
    	FutureTaskHandler fth = new FutureTaskHandler();
    	fth.append(ft1, 0);
    	
    	fth.stopHandler(true);*/
    	
    	P4JServer server = new P4JServer(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
    	server.bind(new InetSocketAddress(8361));
    	server.getPackets().register(PingPongPacket.class, 1);
    	server.setAccepting();
    	
    	server.getEventHandler().register(new Listener<ServerClient>() {
    		@Override
    		public void receive(ReceiveEvent<ServerClient> event) {
    			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
    			System.out.println("--- server read: "+event.getPacket());
    		}
    		@Override
    		public void transmit(TransmitEvent<ServerClient> event) {
    			System.out.println("--- server write: "+event.getPacket());
    		}
		});
    	
    	System.out.println("server done");
    	
    	P4JClient client = new P4JClient(CodecManager.base(), EncryptionManager.raw(), CompressionManager.raw());
    	client.bind();
    	client.getPackets().register(PingPongPacket.class, 1);
    	client.connect(InetAddress.getLocalHost(), server.getLocalInetSocketAddress().getPort());
    	System.out.println(client.getLocalInetSocketAddress());
    	
    	client.getEventHandler().register(new Listener<P4JClient>() {
    		@Override
    		public void receive(ReceiveEvent<P4JClient> event) {
    			System.out.println("--- client read: "+event.getPacket());
    		}
    		@Override
    		public void transmit(TransmitEvent<P4JClient> event) {
    			System.out.println("--- client write: "+event.getPacket());
    		}
		});
    	
    	System.out.println("client done");
    	
    	System.out.println(client.write(new PingPongPacket()));
    	
    	Thread.sleep(5000);
    	
    	client.close();
    	server.close();
    	
    	
    }

}