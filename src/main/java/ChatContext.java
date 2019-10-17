
import com.alibaba.fastjson.JSONObject;

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
    //发送文件时发送的缓冲区的字节大小
    byte[] bytes = new byte[1024];

    //服务器接收的socket类型
    public enum ServerSocketType {
        //关闭socket
        COLSE("close"),

        //单聊
        CHAT_TO_ONE("chatToOne"),

        //群聊
        CHAT_TO_ALL("chatToAll"),

        SEND_FILE("sendFile");


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

        GIVE_NAME("giveName"),

        RECIVE_FILE("reciveFile");
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

    //发送字符
     void sendMessage(String message, Socket socket) throws IOException {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
        printWriter.println(message);
        printWriter.flush();
    }

    //接收字符
    String reciveMessage(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return bufferedReader.readLine();
    }

    //发送二进制流
//    void sendByte(InputStream inputStream) throws IOException {
//        int len  = -1;
//        while ((len = inputStream.read(bytes,0,bytes.lengtff
//    }

    //获取键盘输入字符
    String getKeyboardEntry(String name) {
        System.out.println("发送给" + name + "---------------请输入消息：");
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    //发送文件
    public void sendFile(File file,String name,Socket socket) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        inputStream.read(bytes);
        Request request = new Request();
        request.setSocketType(ServerSocketType.SEND_FILE.getType());
        request.setBytes(bytes);
        request.setName(name);
        request.setFileName(file.getName());
        String requestJson = JSONObject.toJSONString(request);
        sendMessage(requestJson,socket);
    }

    //接收文件
    public void handReciveFile(Request request) throws IOException {
        byte[] bytes = request.getBytes();
        //写入文件
        File file = new File("D:/image");
        if(!file.exists()){
            file.mkdir();
        }
        String filename = request.getFileName();
        File file1 = new File(file.getAbsoluteFile()+"/"+filename);
        OutputStream outputStream = new FileOutputStream(file1);
        outputStream.write(bytes);

    }

}
