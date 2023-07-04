package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class FloatEncoder implements Encoder<Float> {

    public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Float.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public ByteBuffer encode(boolean head, Float obj) {
        ByteBuffer bb = ByteBuffer.allocate(8 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putDouble(obj);
        return bb;
    }

}
