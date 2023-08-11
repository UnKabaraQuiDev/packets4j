package guess_the_number;

import java.nio.ByteBuffer;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.jb.codec.encoder.Encoder;

public class RangeEncoder implements Encoder<Range> {

	public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Range.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }
    
    public ByteBuffer encode(boolean head, Range obj) {
        ByteBuffer bb = ByteBuffer.allocate(1 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        
        bb.putInt(obj.getMin());
        bb.putInt(obj.getMax());
        
        bb.flip();
        return bb;
    }

}
