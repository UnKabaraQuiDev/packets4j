package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class NullEncoder implements Encoder<Object> {

	public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return null;}
    public boolean confirmType(Object obj) {return obj == null;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public ByteBuffer encode(boolean head, Object obj) {
        ByteBuffer bb = ByteBuffer.allocate((head ? 2 : 0));
        if(head)
            bb.putShort(header);
        
        bb.flip();
        return bb;
    }
	
}
