
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
    private byte[] bytes = new byte[1024 * 8];

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
    synchronized void sendMessage(String message, Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        byte[] bytes;
        //首先发送4个字节的长度
        int messageLength = getStringByteLength(message);
        System.out.println("发送的消息长度:" + messageLength);
        bytes = intToByteArray(messageLength);
        outputStream.write(bytes);
        System.out.println("发送的消息:" + message);
        //再发送实体内容
        byte[] messageBytes = message.getBytes("GBK");
        outputStream.write(messageBytes);
    }

    //接收字符
    String[] reciveMessage(Socket socket) throws IOException {
        System.out.println("接收消息");
        String[] strings = new String[1000];
        int i = 0;
        byte[] bytes = new byte[Request.PER_PACAGE_LENGTH];
        //首先接收四个字节的内容，这个内容代表了实体的长度
        InputStream inputStream = socket.getInputStream();
        while ((inputStream.read(bytes)) != -1) {
            int dateLength = byteArrayToInt(bytes);
            System.out.println("消息长度:" + dateLength);
            byte[] dateDytes = new byte[dateLength];
            if (inputStream.read(dateDytes) != -1) {
                strings[i] = new String(dateDytes, "GBK");
                System.out.println(strings[i]);
                i++;
            }
            if (inputStream.available() == 0) {
                break;
            }
        }
        System.out.println("消息提取结束，准备解析");
        return strings;
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
    void sendFile(File file, String name, Socket socket) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        Runnable runnable1 = () -> {
            try {
                int c;
                while ((c = inputStream.read(bytes)) != -1) {
                    byte[] bytes1 = new byte[c];
                    bytes1 = bytes;
                    Request request = new Request();
                    request.setSocketType(ServerSocketType.SEND_FILE.getType());
                    request.setBytes(bytes1);
                    request.setName(name);
                    request.setFileName(file.getName());
                    String requestJson = JSONObject.toJSONString(request);
                    System.out.println("发送的文件:" + requestJson);
                    try {
                        sendMessage(requestJson, socket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        threadPool.submit(runnable1);
    }

    //接收文件
    void handReciveFile(Request request) throws IOException {
        byte[] bytes = request.getBytes();
        //写入文件
        File file = new File("D:/image");
        if (!file.exists()) {
            if (!file.mkdir()) {
                System.out.println("文件夹创建失败");
                return;
            }
        }
        String filename = request.getFileName();
//        File file1 = new File(file.getAbsoluteFile() + "/" + filename);
//        OutputStream outputStream = new FileOutputStream(file1);
//        outputStream.write(bytes);

        // 打开一个随机访问文件流，按读写方式

        RandomAccessFile randomFile = new RandomAccessFile(file.getAbsoluteFile() + "/" + filename, "rw");

        // 文件长度，字节数

        long fileLength = randomFile.length();

        //将写文件指针移到文件尾。在该位置发生下一个读取或写入操作。

        randomFile.seek(fileLength);

        //按字节序列将该字符串写入该文件。

        randomFile.write(bytes);

    }

    private static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    private static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes = new byte[4];
        bytes[3] = 0B00000001;
        bytes[0] = (byte) 0B10000000;
//         bytes[1] = bytes[2] = bytes[3] = (byte) 0B11111111;
        int i = byteArrayToInt(bytes);
        System.out.println(i);
    }

    //获取字符串的字节长度
    private int getStringByteLength(String str) {
        int realLength = 0;
        for (int i = 0; i < str.length(); i++) {
            char charCode = str.charAt(i);
            if (charCode <= 128)
                realLength += 1;
            else
                realLength += 2;
        }
        return realLength;
    }
}
