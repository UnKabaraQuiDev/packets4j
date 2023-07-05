package lu.pcy113.p4j.crypto;

import src.lu.pcy113.p4j.crypto.decryptor.Decryptor;
import src.lu.pcy113.p4j.crypto.encryptor.Encryptor;

public class EncryptionManager {

    private Encryptor encryptor;
    private Decryptor decryptor;

    public EncryptionManager() {
        this(new RawEncryptor(), new RawDecryptor());
    }
    public EncryptionManager(Encryptor e, Decryptor d) {
        this.encryptor = e;
        this.decryptor = d;
    }

    public ByteBuffer decrypt(ByteBuffer b) {return decryptor.decrypt(b);}
    public ByteBuffer encrypt(ByteBuffer b) {return encryptor.encrypt(b);}

    public Encryptor getEncryptor() {return encryptor;}
    public Decryptor getDecryptor() {return decryptor;}
    public void setEncryptor(Encryptor encryptor) {this.encryptor = encryptor;}
    public void setDecryptor(Decryptor decryptor) {this.decryptor = decryptor;}

}
