package lu.pcy113.p4j.encoder;

public interface Encoder<T> {
    short header();
    default String register(CodecManager cm, short header) {
        if(header() != -1 || codecManager() != null)
            throw new IllegalArgumentException("Cannot register Encoder to more than one CodecManager.");
    }
    Class<?> type();
    ByteBuffer encode(boolean head, T obj);
    default int estimateSize(boolean head, T obj) {
        return (head ? 2 : -1);
    }
}
