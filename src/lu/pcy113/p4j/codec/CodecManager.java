package lu.pcy113.p4j.codec;

import java.util.HashMap;
import lu.pcy113.codec.encoder;
import lu.pcy113.codec.decoder;

public class CodecManager {

    private HashMap<Short, Pair<Decoder, String>> registeredDecoders = new HashMap<>();
    private HashMap<String, Pair<Encoder, Short>> registeredEncoders = new HashMap<>();

    public void register(Decoder d, short header) {
        registeredDecoder.put(header, new Pair<>(d, d.register(this, header)));
    }
    public void register(Encoder d, short header) {
        registeredEncoder.put(header, new Pair<>(d, d.register(this, header)));
    }

    public Decoder getEncoder(short header) {
        return registeredDecoders.get(name).getKey();
    }
    public Decoder getDecoder(String name) {
        return registeredEncoders.get(name).getKey();
    }

}