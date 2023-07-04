package lu.pcy113.p4j.encoder;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class MapEncoder implements Encoder<Map<?, ?>> {

    public CodecManager cm;
    public short header;

    public short header() {
        return header;
    }
    public String register(CodecManager cm, short header) {
        this.cm = cm;
        this.header = header;
    }

    /**
     * ( HEAD           2b
     * - SIZE           4b
     * - SUB KEY HEAD   2b
     * - SUB VALUE HEAD 2b
     * - DATA           xb
     */
    public ByteBuffer encode(boolean head, Map<?, ?> obj) {
        Class<?> key, value;
        Type[] interfaces = obj.getClass().getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType() == Map.class) {
                    Type[] typeArguments = parameterizedType.getActualTypeArguments();
                    if (typeArguments.length >= 2) {
                        key = (Class<?>) typeArguments[0];
                        value = (Class<?>) typeArguments[1];
                    }
                }
            }
        }

        if(key == null)
            throw new IllegalArgumentException("Key type of map cannot be null");
        if(value == null)
            throw new IllegalArgumentException("Value type of map cannot be null");

        Encoder<?> keyEncoder = cm.getEncoder(key.getName());
        Encoder<?> valueEncoder = cm.getEncoder(key.getName());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for(Entry<?, ?> o : obj.entrySet()) {
            out.write(keyEncoder.encode(false, o.getKey()));
            out.write(valueEncoder.encoder(false, o.getValue()));
        }
        ByteBuffer bb = ByteBuffer.allocate(out.size() + 4 + (head ? 2 : 0) + 4);
        if(head)
            bb.putShort(header);
        bb.putInt(out.size());
        bb.putShort(keyEncoder.header());
        bb.putShort(valuEncoder.header());
        bb.put(out.toByteArray());
        return bb;
    }

}
