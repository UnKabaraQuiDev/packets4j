package lu.pcy113.p4j.crypto.decryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SymmetricEncryption {
    private static final String ALGORITHM = "AES";

    private SecretKeySpec secretKey;
    private Cipher cipher;

    public SymmetricEncryption(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        secretKey = new SecretKeySpec(key, ALGORITHM);
        cipher = Cipher.getInstance(ALGORITHM);
    }

    public ByteBuffer decrypt(ByteBuffer buffer) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(buffer.array(), buffer.position(), buffer.remaining());
        return ByteBuffer.wrap(decryptedBytes);
    }
}
