import java.security.SecureRandom;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

class UserAuth {
    private static Map<String, UserAuth> usersAndThreads;
    private boolean isAuth;
    public String login;

    static {
        usersAndThreads = new HashMap<>();
    }

    public static UserAuth getCurrentThreadUserAuth() {
        UserAuth ua = null;

        try {
            ua = usersAndThreads.get(Thread.currentThread().getName());
        } catch (NullPointerException ignored) {}

        return ua;
    }

    private static void putThreadUserAuth(UserAuth ua) {
        usersAndThreads.put(Thread.currentThread().getName(), ua);
    }

    public static void register(final String login) throws Exception {
        // System.out.println("Try to register: " + login);

        SecureRandom rand = new SecureRandom();
        rand.setSeed(Clock.systemUTC().millis());

        final byte PASSWORD_SIZE = 7;
        String password = "";
        byte[] bytes = new byte[PASSWORD_SIZE];
        rand.nextBytes(bytes);

        final byte CAPITAL_CHAR_OFFSET = 'Z' - 'A' - 1;
        final byte ORDINARY_CHAR_OFFSET = 'z' - 'a' - 1;
        final long TO_UNSIGNED_BIT_MASK = 0xfffl;

        for (byte b : bytes) {
            byte[] symbolByte = new byte[1];
            rand.nextBytes(symbolByte);
            switch ((byte)(b & TO_UNSIGNED_BIT_MASK) % 2) {
                case 0: password += (char)((symbolByte[0] & TO_UNSIGNED_BIT_MASK) % CAPITAL_CHAR_OFFSET + (byte)'A'); break;
                case 1: password += (char)((symbolByte[0] & TO_UNSIGNED_BIT_MASK) % ORDINARY_CHAR_OFFSET + (byte)'a'); break;
                default: password += (symbolByte[0] & TO_UNSIGNED_BIT_MASK) % 10; break;
            }
        }

        Mailing.send("Регистрация", "Вы успешно зарегистрировались!\nВаш пароль: " + password, login);
        DBUserInteractionable db = (DBUserInteractionable)Server.db;
        db.addUser(login, SHA1.encrypt(password));
        // WTF WHY THIS IS USED
        throw new Exception("password sended to ur email");
    }

    public UserAuth(final String login, final String password) {
        // System.out.println(Thread.currentThread().getName());
        putThreadUserAuth(this);

        // System.out.println("login: " + login);
        // System.out.println("password: " + password);
        this.login = login;

        DBUserInteractionable db = (DBUserInteractionable)Server.db;
        isAuth = db.auth(login, password);
    }

    public boolean isAuth() {
        return isAuth;
    }
}
