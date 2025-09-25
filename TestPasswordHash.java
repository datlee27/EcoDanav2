import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test password "owner123"
        String password = "owner123";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashedPassword);
        
        // Test if the hash from database matches
        String dbHash = "$2a$10$udCgxkgXEJkPLNts47c.vevinawbk2CTSbzhydw3mRP53Eq7mSiri";
        boolean matches = encoder.matches(password, dbHash);
        
        System.out.println("Database hash: " + dbHash);
        System.out.println("Password matches: " + matches);
        
        // Generate new hash for owner123
        String newHash = encoder.encode("owner123");
        System.out.println("New hash for owner123: " + newHash);
    }
}
