
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ChatContext {
    //如果使用多线程，那就需要线程池，防止并发过高时创建过多线程耗尽资源
    static ExecutorService threadPool = Executors.newFixedThreadPool(100);
    int PORT = 6666;
    String ADDRESS = "localhost";

    //服务器接收的socket类型
    public enum ServerSocketType {
        //关闭socket
        COLSE("close"),

        //单聊
        CHAT_TO_ONE("chatToOne"),

        //群聊
        CHAT_TO_ALL("chatToAll");


        private final String Type;

        ServerSocketType(String Type) {
            this.Type = Type;
        }

        public String getType() {
            return Type;
        }
    }

    //客户端接收的socket类型
    public enum ClientSocketType {

        CHAT_WITH_CLIENT("chatWithClient"),

        GIVE_NAME("giveName");
        private final String type;

        ClientSocketType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }


    public abstract void init() throws IOException;

    void close(Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

     void sendMessage(String message, Socket socket) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(message);
        printWriter.flush();
    }


    String reciveMessage(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return bufferedReader.readLine();
    }

    //获取键盘输入字符
    String getKeyboardEntry(String name) {
        System.out.println("发送给" + name + "---------------请输入消息：");
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

}
