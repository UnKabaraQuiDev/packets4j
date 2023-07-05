package lu.pcy113.p4j.compress.compressor;

import java.nio.ByteBuffer;

public class SnappyCompressor implements Compressor {

	@Override
	public ByteBuffer compress(ByteBuffer bb) throws Exception {
		org.apache.hadoop.io.compress.snappy.SnappyCompressor sc = new org.apache.hadoop.io.compress.snappy.SnappyCompressor();
		sc.setInput(bb.array(), 0, bb.limit());
		sc.finish();
		byte[] compressed = new byte[(int) sc.getBytesWritten()];
		sc.compress(compressed, 0, compressed.length);
		return ByteBuffer.wrap(compressed);
	}

}
