package lu.pcy113.p4j.util;

import java.util.ArrayList;
import java.util.List;

public final class ArrayUtils {

	public static final ArrayList<Byte> byteArrayToList(byte[] b) {
		ArrayList<Byte> al = new ArrayList<>(b.length);
		for(int i = 0; i < b.length; i++)
			al.add(b[i]);
		return al;
	}
	
	public static final byte[] byteListToPrimitive(List<Byte> bytes) {
		byte[] b = new byte[bytes.size()];
		for(int i = 0; i < b.length; i++)
			b[i] = bytes.get(i);
		return b;
	}
	
	public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder();
        for (byte b : byteArray) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
	
}
