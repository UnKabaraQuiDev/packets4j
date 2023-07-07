package lu.pcy113.p4j.codec.encoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.util.ArrayUtils;

public class ArrayListEncoder implements Encoder<ArrayList<Object>> {
	
	public CodecManager cm;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return ArrayList.class;}
    public boolean confirmType(Object o) {return o.getClass().isArray();}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    /**
     * ( HEAD     2b
     * - SIZE     4b
     * - SUB HEAD 2b
     * - DATA     xb
     */
    public ByteBuffer encode(boolean head, ArrayList<Object> obj) {
    	String name = obj.getClass().getName();
    	System.out.println("name: "+name);
        Encoder elementEncoder = cm.getEncoder(name.substring(2, name.length()-1));
        if(elementEncoder == null)
        	throw new EncoderNotFoundException("Encoder for object: "+obj.getClass().getName()+", not found in codec.");
        
        List<Byte> elements = new ArrayList<>();
        for(Object o : obj) {
            elements.addAll(ArrayUtils.byteArrayToList(elementEncoder.encode(false, o).array()));
        }
        ByteBuffer bb = ByteBuffer.allocate(elements.size() + 4 + (head ? 2 : 0) + 2);
        if(head)
            bb.putShort(header);
        bb.putInt(obj.size());
        bb.putShort(elementEncoder.header());
        bb.put(ArrayUtils.byteListToPrimitive(elements));
        
        bb.flip();
        return bb;
    }
	
}
