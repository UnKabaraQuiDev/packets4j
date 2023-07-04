package lu.pcy113.p4j.encoder;

import java.lang.Byte;
import java.nio.ByteBuffer;

public class ByteEncoder implements Encoder<Byte> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, byte obj) {
        ByteBuffer bb = ByteBuffer.allocate(1 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.put(obj);
        return bb;
    }

}
