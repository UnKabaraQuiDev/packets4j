package lu.pcy113.p4j.crypto.encryptor;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryptor implements Encryptor {
	private static final String ALGORITHM = "AES";

	private SecretKeySpec secretKey;
	private Cipher cipher;

	public AESEncryptor(byte[] key) throws NoSuchPaddingException, InvalidKeyException {
		try {
			secretKey = new SecretKeySpec(key, ALGORITHM);
			cipher = Cipher.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			/* probably impossible */
		}
	}

	@Override
	public ByteBuffer encrypt(ByteBuffer buffer) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(buffer.array(), buffer.position(), buffer.remaining());
		return ByteBuffer.wrap(encryptedBytes);
	}

}
