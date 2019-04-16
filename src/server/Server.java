import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.StringBuilder;

class Handle extends Thread {
    private Socket socket; // сокет, через который сервер общается с клиентом,
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток записи в сокет
    private static CmdWorker worker;

    static {
        worker = new CmdWorker();
    }

    public Handle(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String str;
        try {
            try {
                while (true) {
                    str = in.readLine();
                    if (str.equals(null)) break;
                    processInput(str);
                }
            } catch (NullPointerException ignored) {}
        } catch (IOException e) {
            this.downService();
        }
    }

    private void getSerializedData(String data) {
        try {
            PriorityQueue<City> pq = (PriorityQueue<City>)Convertr.convertFromByteString(data);
            worker.set(pq);
         } catch (Exception e) {}
    }

    private void processInput(String clientData) {
        // System.out.println('>' + clientData);

        final int LOGIN_OFFSET = -1;
        final int PASSWORD_OFFSET = -2;
        final int FIELDS_SIZE = 2;

        String[] splittedStr = clientData.split(" ");

        String login, password;
        try {
            login = splittedStr[splittedStr.length + LOGIN_OFFSET];
            password = splittedStr[splittedStr.length + PASSWORD_OFFSET];
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < splittedStr.length - FIELDS_SIZE; i++) builder.append(splittedStr[i] + ' ');
            clientData = builder.toString();
        } catch(ArrayIndexOutOfBoundsException e) {
            login = null;
            password = null;
        }

        // проверяем нужна ли регистрация
        splittedStr = clientData.split(" ");
        try {
            if (splittedStr[0].equals("reg")) {
                if (splittedStr.length != 2)
                    send("uncorrect syntax, correct syntax reg <login>");
                else
                    try {
                        UserAuth.register(splittedStr[1]);
                    } catch (Exception e) {
                        send(e.toString().substring(21));
                    }

                return;
            }
        // empty str
        } catch (ArrayIndexOutOfBoundsException ingnored) {}

        // авторизируемся значитца
        UserAuth ua = new UserAuth(login, password);
        if (!ua.isAuth()) {
            send("Uncorrect login or password");
            return;
        }

        final String PREAMBLE = "#####";
        if (clientData.indexOf(PREAMBLE) == 0)
            getSerializedData(clientData.substring(PREAMBLE.length()));
        else {
            String out = worker.doCmd(clientData);
            if (!out.equals(""))
                send(out);
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + '\n');
            out.flush();
        } catch (IOException ignored) {}
    }

    private void downService() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (Handle vr : Server.serverList) {
                    if(vr.equals(this))
                        vr.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }
}

public class Server {
    public static final int PORT = 8080;
    public static LinkedList<Handle> serverList = new LinkedList<>(); // список всех нитей - экземпляров
    // сервера, слушающих каждый своего клиента

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Сервер запущен");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    serverList.add(new Handle(socket));
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}
