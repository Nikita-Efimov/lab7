import java.security.SecureRandom;
import java.time.Clock;

class UserAuth {
    private boolean isAuth;

    public static void register(final String login) {
        System.out.println("Try to register with: " + login);

        SecureRandom rand = new SecureRandom();
        rand.setSeed(Clock.systemUTC().millis());
        // System.out.println(rand.next(10));
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
