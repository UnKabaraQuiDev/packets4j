package lu.pcy113.p4j.codec;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import lu.pcy113.p4j.codec.decoder.*;
import lu.pcy113.p4j.codec.encoder.*;
import lu.pcy113.p4j.util.Pair;

public class CodecManager {

    private HashMap<Short, Pair<Decoder, String>> registeredDecoders = new HashMap<>();
    private HashMap<String, Pair<Encoder, Short>> registeredEncoders = new HashMap<>();

    public void register(Decoder d, short header) {
    	registeredDecoders.put(header, new Pair<>(d, d.register(this, header)));
    }
    public void register(Encoder e, short header) {
    	registeredEncoders.put(e.register(this, header), new Pair<>(e, header));
    }
    public void register(Encoder e, Decoder d, short header) {
    	register(d, header);
    	register(e, header);
    }

    public Decoder getDecoder(short header) {
        return registeredDecoders.get(header).getKey();
    }
    public Encoder getEncoder(String name) {
        return registeredEncoders.get(name).getKey();
    }
    public Encoder getEncoder(Object clazz) {
        if(registeredEncoders.containsKey(clazz.getClass().getName()))
        	return registeredEncoders.get(clazz.getClass().getName()).getKey();
        for(Entry<String, Pair<Encoder, Short>> e : registeredEncoders.entrySet())
        	if(e.getValue().getKey().confirmType(clazz))
        		return e.getValue().getKey();
        return null;
    }
    
    public ByteBuffer encode(Object o) {
    	return getEncoder(o).encode(true, o);
    }
    public Object decode(ByteBuffer bb) {
    	return getDecoder(bb.getShort()).decode(false, bb);
    }
    
    public static final CodecManager base() {
    	CodecManager cm = new CodecManager();
    	
    	cm.register(new ByteEncoder(), new ByteDecoder(), (short) 1);
    	cm.register(new ShortEncoder(), new ShortDecoder(), (short) 2);
    	cm.register(new IntegerEncoder(), new IntegerDecoder(), (short) 3);
    	cm.register(new DoubleEncoder(), new DoubleDecoder(), (short) 4);
    	cm.register(new FloatEncoder(), new FloatDecoder(), (short) 5);
    	cm.register(new CharacterEncoder(), new CharacterDecoder(), (short) 6);
    	cm.register(new StringEncoder(), new StringDecoder(), (short) 7);
    	cm.register(new ArrayEncoder(), new ArrayDecoder(), (short) 8);
    	cm.register(new MapEncoder(), new MapDecoder(), (short) 9);
    	
    	return cm;
    }

}