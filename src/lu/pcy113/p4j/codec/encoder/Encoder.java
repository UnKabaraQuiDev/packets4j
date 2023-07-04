package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public interface Encoder<T> {
	
	CodecManager codecManager();
    short header();
    Class<?> type();
    
    String register(CodecManager cm, short header);
    default void verifyRegister() {
    	if(codecManager() != null)
            throw new IllegalArgumentException("Cannot register Encoder to more than one CodecManager.");
    }
    
    
    ByteBuffer encode(boolean head, T obj);
    
    default int estimateSize(boolean head, T obj) {
        return (head ? 2 : -1);
    }
    default boolean confirmType(Object obj) {
    	return obj.getClass().equals(type());
    }
}
