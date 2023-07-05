package lu.pcy113.p4j.packets.s2c;

public interface S2CPacket<T> extends Packet {

    T serverWrite(ServerClient client);
    void clientRead(ClientServer cserver, T obj);

}