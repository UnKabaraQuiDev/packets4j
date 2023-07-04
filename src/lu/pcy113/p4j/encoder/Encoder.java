package lu.pcy113.p4j.encoder;

public interface Encoder<T> {
    short header();
    String register(CodecManager cm, short header);
    ByteBuffer encode(boolean head, T obj);
    default int estimateSize(boolean head, T obj) {
        return (head ? 2 : 0);
    }
}
