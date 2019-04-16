import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

class Mailing {
    public static void send(final String msgTheme, final String msgContent, final String msgResiver) throws Exception {
        final String username = "lessonsjavavt@gmail.com";
        final String password = "FdfgKJ34";

        Properties prop = new Properties();
		prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(msgResiver)
            );

            message.setSubject(msgTheme);
            message.setText(msgContent);

            Transport.send(message);

            throw new Exception("password sended to ur email");
        } catch (MessagingException e) {
            throw new Exception("sending failed");
            // e.printStackTrace();
        }
    }

}
