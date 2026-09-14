package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** SHA-256(salt || UTF-8(password)); в TEXT храним saltHex:hashHex. */
public final class PasswordHasher {
    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(digest(salt, password));
    }

    public boolean verify(String password, String stored) {
        if (stored == null) return false;
        try {
            // Совместимость с ранее созданными SHA-256 без соли.
            if (stored.matches("[0-9a-fA-F]{64}")) {
                return MessageDigest.isEqual(HexFormat.of().parseHex(stored), digest(new byte[0], password));
            }
            String[] parts = stored.split(":", -1);
            if (parts.length != 2 || parts[0].length() != 32 || parts[1].length() != 64) return false;
            return MessageDigest.isEqual(HexFormat.of().parseHex(parts[1]),
                    digest(HexFormat.of().parseHex(parts[0]), password));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] digest(byte[] salt, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JDK не поддерживает SHA-256.", e);
        }
    }
}
