package lu.pcy113.p4j.test;

import java.nio.ByteBuffer;

import lu.pcy113.p4j.codec.CodecManager;
import lu.pcy113.p4j.util.ArrayUtils;

public class Main {
    
    public static void main(String args[]) {
    	
    	CodecManager cm = CodecManager.base();
    	
    	ByteBuffer bb = cm.encode(new String[] {"a", "ab", "ac", "ad", "ae", "af"});
        bb.flip();
        System.out.println(ArrayUtils.byteArrayToHexString(bb.array()));
        for(Object o : (Object[]) cm.decode(bb)) {
        	System.out.println(o);
        }
    
    }

}