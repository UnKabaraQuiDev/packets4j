package lu.pcy113.p4j.crypto;

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

    public Encryptor getEncryptor() {return encryptor;}
    public Decryptor getDecryptor() {return decryptor;}
    public void setEncryptor(Encryptor encryptor) {this.encryptor = encryptor;}
    public void setDecryptor(Decryptor decryptor) {this.decryptor = decryptor;}

}
