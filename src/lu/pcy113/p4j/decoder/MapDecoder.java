package lu.pcy113.p4j.decoder;

import java.nio.ByteBuffer;
import java.util.Map;

public class MapDecoder implements Decoder<Map<?, ?>> {

    private CodecManager cm;
    private short header;

    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Map.class;}

    public String register(CodecManager cm, short header) {
        super.register(cm, header);

        this.cm = cm;
        this.header = header;

        return type().getName();
    }

    public Map<?, ?> decode(boolean head, ByteBuffer bb) {
        if(head) {
            short nheader = bb.getShort();
            if(nheader != header)
                decoderNotCompatible(nheader, header);
        }

        int length = bb.getInt();
        short kheader = bb.getShort();
        short vheader = bb.getShort();

        Decoder<?> keyDecoder = cm.getDecoder(kheader);
        Decoder<?> valueDecoder = cm.getDecoder(vheader);

        Map<Object, Object> map = new HashMap<keyDecoder.type(), valueDecoder.type()>();

        for(int i = 0; i < length; i++) {
            Object k = keyDecoder.decode(false, bb);
            Object v = keyDecoder.decode(false, bb);
            map.put(k, v);
        }

        return map;
    }

}