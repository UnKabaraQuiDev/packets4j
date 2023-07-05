package lu.pcy113.p4j.socket.server;

public enum ServerStatus {

    OPEN(),
    ACCEPTING(),
    REFUSING(),
    STOPPED(),
    ERROR();

}
