import java.security.SecureRandom;
import java.time.Clock;

class UserAuth {
    private boolean isAuth;

    public static void register(final String login) {
        System.out.println("Try to register with: " + login);

        SecureRandom rand = new SecureRandom();
        rand.setSeed(Clock.systemUTC().millis());

        final byte PASSWORD_SIZE = 7;
        String password = "";
        byte[] bytes = new byte[PASSWORD_SIZE];
        rand.nextBytes(bytes);

        final byte CAPITAL_CHAR_OFFSET = 'Z' - 'A' - 1;
        final byte ORDINARY_CHAR_OFFSET = 'z' - 'a' - 1;

        for (byte b : bytes) {
            byte[] symbolByte = new byte[1];
            rand.nextBytes(symbolByte);
            switch ((byte)(b & 0xfffl) % 2) {
                case 0: password += (char)((symbolByte[0] & 0xfffl) % CAPITAL_CHAR_OFFSET + (byte)'A'); break;
                case 1: password += (char)((symbolByte[0] & 0xfffl) % ORDINARY_CHAR_OFFSET + (byte)'a'); break;
                default: password += (symbolByte[0] & 0xfffl) % 10; break;
            }
        }

        System.out.println(password);
    }

    public UserAuth(final String login, final String password) {
        System.out.println("login: " + login);
        System.out.println("password: " + password);

        isAuth = false;
    }

    public boolean isAuth() {
        return isAuth;
    }
}
