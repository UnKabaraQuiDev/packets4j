package lu.pcy113.p4j.codec.decoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class ArrayDecoder implements Decoder<Object[]> {

    private CodecManager cm = null;
    private short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return null;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return "Array"; //type().getName();
    }

    public Object[] decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
            	Decoder.decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        short elementHeader = bb.getShort();

        Decoder<?> elementDecoder = cm.getDecoder(elementHeader);
        Object[] array = new Object[length];
        for(int i = 0; i < length; i++) {
            array[i] = elementDecoder.decode(false, bb);
        }
        return array;
    }

}