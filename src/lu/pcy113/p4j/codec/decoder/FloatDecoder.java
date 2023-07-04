package lu.pcy113.p4j.codec.decoder;

import java.lang.Float;
import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class FloatDecoder implements Decoder<Float> {
    
    private CodecManager cm = null;
    private short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Float.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public Float decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
            	Decoder.decoderNotCompatible(nheader, header);
        }

        return bb.getFloat();
    }

}
