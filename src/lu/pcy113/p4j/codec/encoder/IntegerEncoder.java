package lu.pcy113.p4j.codec.encoder;

import java.lang.Integer;
import java.nio.ByteBuffer;

public class IntegerEncoder implements Encoder<Integer> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, int obj) {
        ByteBuffer bb = ByteBuffer.allocate(4 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putInt(obj);
        return bb;
    }

}
