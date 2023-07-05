package lu.pcy113.p4j.crypto.encryptor;

public class RawEncryptor implements Encryptor {

    public ByteBuffer encrypt(ByteBuffer in) {
        return in;
    }

}