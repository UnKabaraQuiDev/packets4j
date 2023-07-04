package lu.pcy113.p4j.encoder;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ArrayEncoder implements Encoder<Object[]> {

    public CodecManager cm;
    public short header;

    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    /**
     * ( HEAD     2b
     * - SIZE     4b
     * - SUB HEAD 2b
     * - DATA     xb
     */
    public ByteBuffer encode(boolean head, Object[] obj) {
        Encoder<?> elementEncoder = cm.getEncoder(obj.getClass().arrayType().getName());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(Object o : obj) {
            out.write(elementEncoder.encode(false, o));
        }
        ByteBuffer bb = ByteBuffer.allocate(out.size() + 4 + (head ? 2 : 0) + 2);
        if(head)
            bb.putShort(header);
        bb.putInt(out.size());
        bb.putShort(elementEncoder.header());
        bb.put(out.toByteArray());
        return bb;
    }

}
