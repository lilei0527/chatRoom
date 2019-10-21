
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ChatContext {
    //如果使用多线程，那就需要线程池，防止并发过高时创建过多线程耗尽资源
    static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    public abstract void init() throws IOException;

    void close(Socket socket) throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    //发送字符
    synchronized void sendMessage(String message, Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        //首先发送4个字节的长度
        int messageLength = Util.getStringByteLength(message);
        byte[] bytes = Util.intToByteArray(messageLength);
        outputStream.write(bytes);
        //再发送实体内容
        byte[] messageBytes = message.getBytes("GBK");
        outputStream.write(messageBytes);
    }

    //接收字符
    List<String> reciveMessage(Socket socket) throws IOException {
        List<String> list = new ArrayList<>();
        byte[] bytes = new byte[Constant.PER_PACAGE_LENGTH];
        //首先接收四个字节的内容，这个内容代表了实体的长度
        InputStream inputStream = socket.getInputStream();
        while ((inputStream.read(bytes)) != -1) {
            int dateLength = Util.byteArrayToInt(bytes);
            byte[] dateDytes = new byte[dateLength];
            if (inputStream.read(dateDytes) != -1) {
                String s = new String(dateDytes, "GBK");
                list.add(s);
            }
            //判断流是否到达末尾
            if (inputStream.available() == 0) {
                break;
            }
        }
        return list;
    }


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
                byte[] bytes = new byte[Constant.FILE_BUFFER_SIZE];
                while (inputStream.read(bytes) != -1) {
                    Request request = new Request();
                    request.setSocketType(Constant.ServerSocketType.SEND_FILE.getType());
                    request.setBytes(bytes);
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
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        threadPool.submit(runnable1);
    }

    //接收文件
    void handleReciveFile(Request request) throws IOException {
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
        // 打开一个随机访问文件流，按读写方式
        RandomAccessFile randomFile = new RandomAccessFile(file.getAbsoluteFile() + "/" + filename, "rw");
        // 文件长度，字节数
        long fileLength = randomFile.length();
        //将写文件指针移到文件尾。在该位置发生下一个读取或写入操作。
        randomFile.seek(fileLength);
        //按字节序列将该字符串写入该文件。
        randomFile.write(bytes);
    }
}
