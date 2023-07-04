package lu.pcy113.p4j.codec.decoder;

import java.lang.Byte;
import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class ByteDecoder implements Decoder<Byte> {
    
	public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Byte.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public Byte decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                Decoder.decoderNotCompatible(nheader, header);
        }

        return bb.get();
    }

}
