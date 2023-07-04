package lu.pcy113.p4j.encoder;

import java.lang.String;
import java.nio.ByteBuffer;

public class StringEncoder implements Encoder<String> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, String obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length() + 4 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putInt(obj.length());
        bb.put(obj.getBytes());
        return bb;
    }

}
