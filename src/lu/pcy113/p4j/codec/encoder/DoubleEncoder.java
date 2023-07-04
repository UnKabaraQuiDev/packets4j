package lu.pcy113.p4j.codec.encoder;

import java.lang.Double;
import java.nio.ByteBuffer;

public class DoubleEncoder implements Encoder<Double> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, double obj) {
        ByteBuffer bb = ByteBuffer.allocate(8 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putDouble(obj);
        return bb;
    }

}
