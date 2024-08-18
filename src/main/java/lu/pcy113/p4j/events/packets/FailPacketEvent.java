package lu.pcy113.p4j.events.packets;

public interface FailPacketEvent extends PacketEvent {

	Throwable getException();

}
