package lu.pcy113.p4j.codec.decoder;

import java.nio.ByteBuffer;
import java.util.List;

public class MapDecoder implements Decoder<Map<?, ?>> {

    private CodecManager cm;
    private short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Array.class;}
    
    public String register(CodecManager cm, short header) {
        super.register(cm, header);

        this.cm = cm;
        this.header = header;
    }

    public Object[] decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        int elementHeader = bb.getShort();

        Decoder<?> elementDecoder = cm.getDecoder(elementHeader);
        Object[] array = new elementDecoder.type()[length];
        for(int i = 0; i < length; i++) {
            array[i] = elementDecoder.decode(false, bb);
        }
        return array;
    }

}