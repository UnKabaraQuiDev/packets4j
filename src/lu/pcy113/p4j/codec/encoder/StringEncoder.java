package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class StringEncoder implements Encoder<String> {

    public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return String.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public ByteBuffer encode(boolean head, String obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length() + 4 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putInt(obj.length());
        bb.put(obj.getBytes());
        
        bb.flip();
        return bb;
    }

}
