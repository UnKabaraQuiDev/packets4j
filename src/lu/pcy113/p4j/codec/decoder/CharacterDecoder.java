package lu.pcy113.p4j.codec.decoder;

import java.lang.Character;
import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;

public class CharacterDecoder implements Decoder<Character> {
    
	public CodecManager cm = null;
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

    public Character decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
            	Decoder.decoderNotCompatible(nheader, header);
        }

        return bb.getChar();
    }

}
