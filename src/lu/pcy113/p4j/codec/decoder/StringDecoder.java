package lu.pcy113.p4j.codec.decoder;

import java.lang.String;
import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class StringDecoder implements Decoder<String> {
    
    private CodecManager cm = null;
    private short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return String.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public String decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                Decoder.decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        byte[] b = new byte[length];
        bb.get(b);
        return new String(b);
    }

}
