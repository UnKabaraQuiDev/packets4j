package lu.pcy113.p4j.codec.decoder;

public interface Decoder<T> {
    short header();
    CodecManager codecManager();
    default String register(CodecManager cm, short header) {
        if(header() != -1 || codecManager() != null)
            throw new IllegalArgumentException("Cannot register Decoder to more than one CodecManager.");
        return null;
    }
    Class<?> type();
    T decode(boolean header, ByteBuffer bb);
    default int estimateSize(boolean head, T obj) {
        return (head ? 2 : -1);
    }
}
