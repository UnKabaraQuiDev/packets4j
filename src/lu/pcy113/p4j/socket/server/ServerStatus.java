package lu.pcy113.p4j.socket.server;

public enum ServerStatus {

    PRE(),
    OPEN(),
    ACCEPTING(),
    REFUSING(),
    STOPPED(),
    ERROR();

}
