package lu.pcy113.p4j.compress.decompressor;

import java.nio.ByteBuffer;

public class SnappyDecompressor implements Decompressor {

	@Override
	public ByteBuffer decompress(ByteBuffer bb) throws Exception {
		org.apache.hadoop.io.compress.snappy.SnappyDecompressor sc = new org.apache.hadoop.io.compress.snappy.SnappyDecompressor();
		sc.setInput(bb.array(), 0, bb.limit());
		sc.end();
		byte[] decompressed = new byte[(int) sc.getRemaining()];
		sc.decompress(decompressed, 0, decompressed.length);
		return ByteBuffer.wrap(decompressed);
	}

}
