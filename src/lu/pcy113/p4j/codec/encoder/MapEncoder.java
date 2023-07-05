package lu.pcy113.p4j.codec.encoder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.util.ArrayUtils;

public class MapEncoder implements Encoder<Map<?, ?>> {

    public CodecManager cm = null;
    public short header;
    
    public CodecManager codecManager() {return cm;}
    public short header() {return header;}
    public Class<?> type() {return Map.class;}
    
    public String register(CodecManager cm, short header) {
    	verifyRegister();
    	
        this.cm = cm;
        this.header = header;
        
        return type().getName();
    }

    /**
     * ( HEAD           2b
     * - SIZE           4b
     * - SUB KEY HEAD   2b
     * - SUB VALUE HEAD 2b
     * - DATA           xb
     */
    public ByteBuffer encode(boolean head, Map<?, ?> obj) {
        Class<?> key = null, value = null;
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

        Encoder keyEncoder = cm.getEncoder(key.getName());
        Encoder valueEncoder = cm.getEncoder(key.getName());

        List<Byte> elements = new ArrayList<>();
        for(Entry<?, ?> o : obj.entrySet()) {
        	elements.addAll(ArrayUtils.byteArrayToList(keyEncoder.encode(false, o.getKey()).array()));
        	elements.addAll(ArrayUtils.byteArrayToList(valueEncoder.encode(false, o.getValue()).array()));
        }
        ByteBuffer bb = ByteBuffer.allocate(obj.size() + 4 + (head ? 2 : 0) + 4);
        if(head)
            bb.putShort(header);
        bb.putInt(obj.size());
        bb.putShort(keyEncoder.header());
        bb.putShort(valueEncoder.header());
        bb.put(ArrayUtils.byteListToPrimitive(elements));
        
        bb.flip();
        return bb;
    }

}
