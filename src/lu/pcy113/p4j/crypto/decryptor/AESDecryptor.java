package lu.pcy113.p4j.crypto.decryptor;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESDecryptor implements Decryptor {
	private static final String ALGORITHM = "AES";

	private SecretKeySpec secretKey;
	private Cipher cipher;

	public AESDecryptor(byte[] key) throws NoSuchPaddingException, InvalidKeyException {
		try {
			secretKey = new SecretKeySpec(key, ALGORITHM);
			cipher = Cipher.getInstance(ALGORITHM);
		}catch(NoSuchAlgorithmException e) {
			/* probably impossible */
		}
	}

	@Override
	public ByteBuffer decrypt(ByteBuffer buffer) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decryptedBytes = cipher.doFinal(buffer.array(), buffer.position(), buffer.remaining());
		return ByteBuffer.wrap(decryptedBytes);
	}
}
