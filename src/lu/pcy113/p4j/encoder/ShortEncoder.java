package lu.pcy113.p4j.encoder;

import java.lang.Short;
import java.nio.ByteBuffer;

public class ShortEncoder implements Encoder<Short> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, short obj) {
        ByteBuffer bb = ByteBuffer.allocate(2 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putShort(obj);
        return bb;
    }

}
