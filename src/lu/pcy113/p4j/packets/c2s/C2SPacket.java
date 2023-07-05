package lu.pcy113.p4j.packets.c2s;

public interface C2SPacket<T> extends Packet {

    T clientWrite(P4JClient client);
    void serverRead(ServerClient sclient, T obj);

}