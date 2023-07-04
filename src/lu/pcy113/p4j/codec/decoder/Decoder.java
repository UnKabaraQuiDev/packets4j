package lu.pcy113.p4j.codec.decoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public interface Decoder<T> {
	
    short header();
    CodecManager codecManager();
    Class<?> type();
    
    String register(CodecManager cm, short header);
    default void verifyRegister() {
    	if(codecManager() != null)
            throw new IllegalArgumentException("Cannot register Decoder to more than one CodecManager.");
    }
    
    T decode(boolean header, ByteBuffer bb);
    
    default int estimateSize(boolean head, T obj) {
        return (head ? 2 : -1);
    }
    static void decoderNotCompatible(short nheader, short header) throws DecoderNotCompatibleException {
    	if(nheader != header)
    		throw new DecoderNotCompatibleException("Decoder not compatible with header: "+nheader+"; Header: "+header);
    }
}
