package guess_the_number;

import java.nio.ByteBuffer;

import lu.pcy113.jb.codec.CodecManager;
import lu.pcy113.jb.codec.decoder.Decoder;

public class RangeDecoder implements Decoder<Range> {

	public CodecManager cm = null;
	public short header;

	public CodecManager codecManager() {return cm;}
	public short header() {return header;}
	public Class<?> type() {return Range.class;}
	
	public String register(CodecManager cm, short header) {
		verifyRegister();
		
		this.cm = cm;
		this.header = header;
		
		return type().getName();
	}
	
	@Override
	public Range decode(boolean head, ByteBuffer bb) {
		if(head) {
			short nheader = bb.getShort();
			if(nheader != header)
				Decoder.decoderNotCompatible(nheader, header);
		}

		return new Range(bb.getInt(), bb.getInt());
	}
	
}
