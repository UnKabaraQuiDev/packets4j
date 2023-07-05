package lu.pcy113.p4j.packets;

public interface Packet {
    
    int id();
    PacketManager packetManager();
    String register(PacketManager pm, int id);
    
}