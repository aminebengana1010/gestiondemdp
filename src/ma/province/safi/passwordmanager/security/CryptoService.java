package ma.province.safi.passwordmanager.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;

    public CryptoService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public CryptoService(byte[] keyBytes) {
        this(new javax.crypto.spec.SecretKeySpec(keyBytes, "AES"));
    }

    public ChiffrementResultat chiffrer(String texteClair) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] chiffre = cipher.doFinal(texteClair.getBytes(StandardCharsets.UTF_8));

        return new ChiffrementResultat(
            Base64.getEncoder().encodeToString(chiffre),
            Base64.getEncoder().encodeToString(iv)
        );
    }

    public String dechiffrer(String texteChiffre, String ivBase64) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(
            GCM_TAG_LENGTH,
            Base64.getDecoder().decode(ivBase64)
        );
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] clair = cipher.doFinal(Base64.getDecoder().decode(texteChiffre));
        return new String(clair, StandardCharsets.UTF_8);
    }

    public static SecretKey genererCle() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);
        return generator.generateKey();
    }

    public static String cleToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey base64ToCle(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new javax.crypto.spec.SecretKeySpec(bytes, "AES");
    }

    public record ChiffrementResultat(String secretChiffre, String iv) {}
}
