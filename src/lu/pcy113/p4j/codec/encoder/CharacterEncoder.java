package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class CharacterEncoder implements Encoder<Character> {

    public CodecManager cm;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Character.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public ByteBuffer encode(boolean head, Character obj) {
        ByteBuffer bb = ByteBuffer.allocate(1 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putChar(obj);
        
        bb.flip();
        return bb;
    }

}
