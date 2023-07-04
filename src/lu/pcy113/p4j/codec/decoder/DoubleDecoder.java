package lu.pcy113.p4j.codec.decoder;

import java.lang.Double;
import java.nio.ByteBuffer;

public class DoubleDecoder implements Decoder<Double> {
    
    private CodecManager cm;
    private short header = -1;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Dobule.class;}
    
    public String register(CodecManager cm, short header) {
        super.register(cm, header);

        this.cm = cm;
        this.header = header;
    }

    public double decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                decoderNotCompatible(nheader, header);
        }

        return bb.getDouble();
    }

}
