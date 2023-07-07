package lu.pcy113.p4j.codec.decoder;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import lu.pcy113.p4j.codec.CodecManager;

public class ArrayListDecoder implements Decoder<ArrayList<Object>> {

	public CodecManager cm = null;
    public short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return ArrayList.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    public ArrayList<Object> decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
            	Decoder.decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        short elementHeader = bb.getShort();

        Decoder<?> elementDecoder = cm.getDecoder(elementHeader);
        ArrayList<Object> array = new ArrayList<>();
        for(int i = 0; i < length; i++) {
            array.add(elementDecoder.decode(false, bb));
        }
        return array;
    }

}
