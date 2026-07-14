package ma.province.safi.passwordmanager.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 600_000;
    private static final int KEY_LENGTH = 256;

    public static HashResult hacher(String motDePasse) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] sel = new byte[16];
        random.nextBytes(sel);

        KeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        return new HashResult(
            Base64.getEncoder().encodeToString(hash),
            Base64.getEncoder().encodeToString(sel)
        );
    }

    public static boolean verifier(String motDePasse, String hashBase64, String selBase64)
            throws Exception {

        byte[] sel = Base64.getDecoder().decode(selBase64);

        KeySpec spec = new PBEKeySpec(motDePasse.toCharArray(), sel, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();

        String hashCalcule = Base64.getEncoder().encodeToString(hash);
        return hashCalcule.equals(hashBase64);
    }

    public record HashResult(String hash, String sel) {}
}
