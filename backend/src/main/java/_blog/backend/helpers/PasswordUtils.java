package _blog.backend.helpers;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // hash password
    public static String hashPassword(String plainPassword) {
        // 12 = work factor (cost). Higher = slower = more secure
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // verify password
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
