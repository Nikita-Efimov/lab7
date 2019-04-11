import java.security.SecureRandom;
import java.time.Clock;

class UserAuth {
    private boolean isAuth;

    public static void register(final String login) {
        SecureRandom rand = new SecureRandom();
        rand.setSeed(Clock.systemUTC().millis());
        // System.out.println(rand.next(10));
    }

    public UserAuth(final String login, final String password) {
        System.out.println("login: " + login);
        System.out.println("password: " + password);
        // for (int i = 0; i < 10; i++) UserAuth.register("kek");

        isAuth = false;
    }

    public boolean isAuth() {
        return isAuth;
    }
}
