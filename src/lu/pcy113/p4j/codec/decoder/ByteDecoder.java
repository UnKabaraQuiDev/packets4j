package lu.pcy113.p4j.codec.decoder;

import java.lang.Byte;
import java.nio.ByteBuffer;

public class ByteDecoder implements Decoder<Byte> {
    
    private CodecManager cm;
    private short header = -1;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Byte.class;}
    
    public String register(CodecManager cm, short header) {
        super.register(cm, header);

        this.cm = cm;
        this.header = header;
    }

    public byte decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                decoderNotCompatible(nheader, header);
        }

        return bb.get();
    }

}
