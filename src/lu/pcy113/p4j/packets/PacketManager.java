package lu.pcy113.p4j.packets;

import java.util.HashMap;

import lu.pcy113.p4j.packets.c2s.C2SPacket;
import lu.pcy113.p4j.packets.s2c.S2CPacket;
import lu.pcy113.p4j.util.Pair;

public class PacketManager {

    private P4JInstance type;

    public PacketManager(P4JInstance instance) {
        type = instance;
    }

    private HashMap<Integer, src.lu.pcy113.p4j.util.Pair<Packet, Class<Packet>>> inPackets = new HashMap<>();
    private HashMap<String, Pair<Packet, Integer>> outPackets = new HashMap<>();

    public void register(Packet p, int id, boolean newInstance) {
        if(type instanceof P4JServer) {
            if(p instanceof S2CPacket)
                outPackets.put(p.register(this, id), new Pair<>(p, id));
            else if(p instanceof C2SPacket)
                inPackets.put(id, new Pair<>(p, p.register(this, id)));
        }else if(type instanceof P4JClient) {
            if(p instanceof S2CPacket)
                inPackets.put(id, new Pair<>(p, newInstance ? Class.forName(p.register(this, id)) : null));
            else if(p instanceof C2SPacket)
                outPacket.put(p.register(this, id), new Pair<>(p, id));
        }
    }

    public Packet packetInstance(int id) {
        if(!inPackets.containsKey(id))
            throw new UnknownPacketId("Packet with id: "+id+"; not registered in PacketManager");
        
        Pair<Packet, Class<Packet>> pair = inPackets.get(id);
        if(pair.getValue() == null)
            return pair.getKey();
        else
            return pair.getValue();
    }

} 