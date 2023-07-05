package lu.pcy113.p4j.packets.s2c;

import src.lu.pcy113.p4j.socket.client.P4JClient;

public interface S2CPacket<T> extends Packet {

    T serverWrite(ServerClient client);
    void clientRead(P4JClient client, T obj);

}