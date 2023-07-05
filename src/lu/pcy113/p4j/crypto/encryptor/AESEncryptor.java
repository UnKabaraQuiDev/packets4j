package lu.pcy113.p4j.crypto.enryptor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AESEncryptor {
    private static final String ALGORITHM = "AES";

    private SecretKeySpec secretKey;
    private Cipher cipher;

    public SymmetricEncryption(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        secretKey = new SecretKeySpec(key, ALGORITHM);
        cipher = Cipher.getInstance(ALGORITHM);
    }

    public ByteBuffer encrypt(ByteBuffer buffer) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(buffer.array(), buffer.position(), buffer.remaining());
        return ByteBuffer.wrap(encryptedBytes);
    }

}
