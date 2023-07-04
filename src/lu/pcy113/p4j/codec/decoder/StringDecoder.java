package lu.pcy113.p4j.codec.decoder;

import java.lang.String;
import java.nio.ByteBuffer;

public class StringDecoder implements Decoder<String> {
    
    private CodecManager cm;
    private short header = -1;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return String.class;}
    
    public String register(CodecManager cm, short header) {
        super.register(cm, header);

        this.cm = cm;
        this.header = header;
    }

    public String decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        return new String(bb.get(new byte[length]));
    }

}
