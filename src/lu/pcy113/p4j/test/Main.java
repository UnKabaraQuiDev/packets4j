package lu.pcy113.p4j.test;

import lu.pcy113.p4j.decoder.*;
import lu.pcy113.p4j.encoder.*;
import java.nio.ByteBuffer;

public class Main {
    
    public static void main(String args[]) {
        ByteBuffer bb = new DoubleEncoder().encode(true, 5.92);
        bb.flip();
        System.out.println(new DoubleDecoder().decode(true, bb));
    }

}