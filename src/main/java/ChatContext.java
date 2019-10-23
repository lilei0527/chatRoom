
import com.alibaba.fastjson.JSON;
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

    //发送文件总长和文件名
    void sendFileTotalLengthAndFileName(File file,Socket socket) throws IOException {
        Request request = new Request();
        request.setSocketType(Constant.SocketType.RECIVE_FILE_TOTOL_LENGTH_AND_FILE_NAME.getType());
        request.setFileName(file.getName());
        request.setTotalFileLength(file.length());
        String requestJson = JSONObject.toJSONString(request);
        sendMessage(requestJson,socket);
    }

    //发送文件
    void sendFile(File file, String name, Socket socket, String socketType) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        Runnable runnable1 = () -> {
            try {
                int c;
                int i = 0;
                byte[] bytes = new byte[Constant.FILE_BUFFER_SIZE];
                while ((c = inputStream.read(bytes)) != -1) {
                    byte[] newBytes = Util.shortByteArray(bytes, c);
                    Request request = new Request();
                    request.setSocketType(socketType);
                    request.setBytes(newBytes);
                    request.setName(name);
                    request.setFileName(file.getName());
                    String requestJson = JSONObject.toJSONString(request);
                    System.out.println("发送的文件:" + requestJson);
                    try {
                        sendMessage(requestJson, socket);
                        i++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("处理的请求数:" + i);
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

    //文件断点传输时，发送端获取到接收方已接收的文件长度并从指定的文件长度处发送文件
    void reciveAndSendRandomFile(File file, Socket socket, Request request) {
        Runnable runnable1 = () -> {
            try {
                //获取接受者接收的文件长度
                long recivedFileLength = request.getRecivedFileLength();
                // 打开一个随机访问文件流，按读写方式
                RandomAccessFile randomFile = new RandomAccessFile(file.getAbsoluteFile() + "/" + request.getFileName(), "rw");
                randomFile.seek(recivedFileLength);
                //将文件发出
                byte[] bytes = new byte[Constant.FILE_BUFFER_SIZE];
                int c = randomFile.read(bytes);
                byte[] newBytes = Util.shortByteArray(bytes, c);
                Request request1 = new Request();
                request1.setBytes(newBytes);
                request1.setFileName(file.getName());
                request1.setTotalFileLength(file.length());
                request1.setSocketType(Constant.SocketType.RECIVE_FILE.getType());
                String requestJson = JSONObject.toJSONString(request1);
                System.out.println("发送的断点文件:" + requestJson);
                sendMessage(requestJson, socket);
                randomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        threadPool.submit(runnable1);
    }


    //接收文件
    void handleReciveFile(Request request, String tempSavePosition,String savePosition,Socket socket) throws IOException {
        byte[] bytes = request.getBytes();

        File tempFileDir = new File(tempSavePosition);
        if (!tempFileDir.exists()) {
            if (!tempFileDir.mkdir()) {
                System.out.println("临时文件夹创建失败");
                return;
            }
        }

        File saveFileDir = new File(savePosition);
        if (!saveFileDir.exists()) {
            if (!saveFileDir.mkdir()) {
                System.out.println("保存文件夹创建失败");
                return;
            }
        }

        String filename = request.getFileName();
        // 打开一个随机访问文件流，按读写方式
        RandomAccessFile randomFile = new RandomAccessFile(tempFileDir.getAbsoluteFile() + "/" + filename, "rw");
        // 文件长度，字节数
        long fileLength = randomFile.length();
        //将写文件指针移到文件尾。在该位置发生下一个读取或写入操作。
        randomFile.seek(fileLength);
        //按字节序列将该字符串写入该文件。
        randomFile.write(bytes);
        long tempFileLength = randomFile.length();
        randomFile.close();

        //如果文件传输完毕，生成主文件，删除临时文件
        if (tempFileLength == request.getTotalFileLength()) {
            File tempFile = new File(tempSavePosition+ "/" + filename);
            File saveFile = new File(savePosition+ "/" + filename);
            copyFile(tempFile,saveFile);
            tempFile.delete();
        }else {
            //如果文件没有传输完毕，向发送方发送已接收的文件长度
            Request request1 = new Request();
            request1.setSocketType(Constant.SocketType.RECIVE_SENDED_FILE_LENGTH.getType());
            request1.setFileName(request.getFileName());
            request1.setRecivedFileLength(tempFileLength);
            String requestJson = JSON.toJSONString(request1);
            sendMessage(requestJson,socket);
        }
    }

    //将一个文件写入另一个文件
    private void copyFile(File srcFile, File posFile) throws IOException {
        InputStream inputStream = new FileInputStream(srcFile);
        OutputStream outputStream = new FileOutputStream(posFile);
        byte[] bytes = new byte[Constant.MOVE_FILE_BUFFER_SIZE];
        int c;
        while ((c = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, c);
        }
    }
}
