package lu.pcy113.p4j.packets.s2c;

public interface S2CPacket<T> extends Packet {

    int id();

    T serverWrite(P4JClient client);
    void clientRead(ClientServer cserver, T obj);

}