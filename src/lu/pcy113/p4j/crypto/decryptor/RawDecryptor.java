package lu.pcy113.p4j.crypto.decryptor;

public class RawDecryptor implements Decryptor {

    @Override
    public ByteBuffer decrypt(ByteBuffer in) {
        return in;
    }

}