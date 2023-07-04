package lu.pcy113.p4j.codec.encoder;

import java.lang.Character;
import java.nio.ByteBuffer;

public class CharEncoder implements Encoder<Character> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    public ByteBuffer encode(boolean head, chat obj) {
        ByteBuffer bb = ByteBuffer.allocate(obj.length() + 4 + (head ? 2 : 0));
        if(head)
            bb.putShort(header);
        bb.putChar(obj);
        return bb;
    }

}
