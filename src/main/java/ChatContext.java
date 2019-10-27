
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.regexp.internal.RE;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ChatContext {
    //如果使用多线程，那就需要线程池，防止并发过高时创建过多线程耗尽资源
    static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    private Map<String, Map<String, FileAttribute>> inCompleteFileMap = new HashMap<>();

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
    String getKeyboardEntry(String s) {
        System.out.println(s);
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }


    //文件断点传输时，发送端获取到接收方已接收的文件长度并从指定的文件长度处发送文件
    void reciveAndSendRandomFile(Socket socket, Request request, String name) {
        try {
            if (!request.isCompleted()) {
                //获取文件路径+文件名
                String filePath = request.getSrcPath();
                File file = new File(filePath);
                //获取接受者接收的文件长度
                long recivedFileLength = request.getRecivedFileLength();
                if (recivedFileLength < file.length()) {
                    sendRandomFile(file, recivedFileLength, socket, name);
                }
            } else {
                System.out.println("文件已传输完成");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //发送可断点文件
    void sendRandomFile(File file, long recivedFileLength, Socket socket, String username) throws IOException {
        Runnable runnable1 = () -> {
            // 打开一个随机访问文件流，按读写方式
            RandomAccessFile randomFile = null;
            try {
                randomFile = new RandomAccessFile(file, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                randomFile.seek(recivedFileLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //将文件发出
            byte[] bytes = new byte[Constant.FILE_BUFFER_SIZE];
            int c = 0;
            try {
                c = randomFile.read(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] newBytes = Util.shortByteArray(bytes, c);
            Request request1 = new Request();
            request1.setUsername(username);
            request1.setBytes(newBytes);
            request1.setFileName(file.getName());
            request1.setTotalFileLength(file.length());
            request1.setSrcPath(file.getPath());
            request1.setSocketType(Constant.SocketType.RECIVE_FILE.getType());
            String requestJson = JSONObject.toJSONString(request1);
            System.out.println("发送的断点文件:" + requestJson);
            try {
                sendMessage(requestJson, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                randomFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        threadPool.submit(runnable1);
    }


    //接收文件
    long handleReciveFile(Request request, String tempSavePosition, String savePosition, String inCompleteFileMapSavePosition, Socket socket) throws Exception {
        System.out.println("接受的request：" + request.toString());
        createFileDir(tempSavePosition);
        createFileDir(savePosition);
        createFileDir(inCompleteFileMapSavePosition);
        long tempFileLength = writeToRandomFile(request.getFileName(), tempSavePosition, request.getBytes());

        sendRecivedLength(request, tempFileLength, socket);

        //更新completed字段
        if (tempFileLength == getFileTotalLength(request)) {
            FileAttribute fileAttribute = new FileAttribute();
            Map<String, FileAttribute> map = inCompleteFileMap.get(request.getUsername());
            FileAttribute fileAttribute1 = map.get(request.getFileName());
            fileAttribute.setCompleted(true);
            fileAttribute.setExpireDate(Util.getFileExpireTime());
            fileAttribute.setPath(fileAttribute1.getPath());
            fileAttribute.setFileName(fileAttribute1.getFileName());
            fileAttribute.setReciveName(fileAttribute1.getReciveName());
            fileAttribute.setTotalLength(fileAttribute1.getTotalLength());
            Map<String, FileAttribute> map1 = new HashMap<>();
            map1.put(request.getFileName(), fileAttribute);
            map.putAll(map1);
            inCompleteFileMap.put(request.getUsername(), map);
        }

        //将未完成文件的信息写入文件
        String inCompleteFileJson = JSON.toJSONString(inCompleteFileMap);
        saveToFile(inCompleteFileJson, inCompleteFileMapSavePosition);
        System.out.println("写入文件后的文件长度" + tempFileLength);

        return tempFileLength;
    }

    //将一个文件写入另一个文件
    void copyFile(File srcFile, File posFile) throws IOException {
        InputStream inputStream = new FileInputStream(srcFile);
        OutputStream outputStream = new FileOutputStream(posFile);
        byte[] bytes = new byte[Constant.MOVE_FILE_BUFFER_SIZE];
        int c;
        while ((c = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, c);
        }
        inputStream.close();
        outputStream.close();
    }

    //将内存中的保存成文件
    private void saveToFile(String toSaveString, String savePath) throws IOException {
        byte[] bytes = toSaveString.getBytes();
        OutputStream outputStream = new FileOutputStream(savePath + "/" + ClientConstant.IN_COMPLETE_FILE_MAP_NAME);
        outputStream.write(bytes);
        outputStream.close();
    }


    //获取文件的总长度
    long getFileTotalLength(Request request) {
        Map<String, FileAttribute> map = inCompleteFileMap.get(request.getUsername());
        FileAttribute fileAttribute1 = new FileAttribute();
        //文件传输开始
        fileAttribute1.setExpireDate(Util.getFileExpireTime());
        fileAttribute1.setPath(request.getSrcPath());
        fileAttribute1.setFileName(request.getFileName());
        fileAttribute1.setCompleted(false);
        fileAttribute1.setReciveName(request.getName());
        fileAttribute1.setTotalLength(request.getTotalFileLength());
        Map<String, FileAttribute> map1 = new HashMap<>();
        map1.put(request.getFileName(), fileAttribute1);
        if (map == null) {
            inCompleteFileMap.put(request.getUsername(), map1);
        } else {
            FileAttribute fileAttribute = map.get(request.getFileName());
            if (fileAttribute == null) {
                map.putAll(map1);
                inCompleteFileMap.put(request.getUsername(),map);
            }
        }

        return inCompleteFileMap.get(request.getUsername()).get(request.getFileName()).getTotalLength();
    }


    //创建文件夹
    private void createFileDir(String dirName) {
        File fileDir = new File(dirName);
        if (!fileDir.exists()) {
            if (!fileDir.mkdir()) {
                System.out.println("临时文件夹创建失败");
            }
        }
    }

    //写入追加的临时文件
    private long writeToRandomFile(String fileName, String savePosition, byte[] bytes) throws IOException {
        // 打开一个随机访问文件流，按读写方式
        RandomAccessFile randomFile = new RandomAccessFile(savePosition + "/" + fileName, "rw");
        // 文件长度，字节数
        long fileLength = randomFile.length();
        System.out.println("目前的文件的长度" + fileLength);
        //将写文件指针移到文件尾。在该位置发生下一个读取或写入操作。
        randomFile.seek(fileLength);
        //按字节序列将该字符串写入该文件。
        if (bytes != null) {
            randomFile.write(bytes);
        }
        //写入文件后的文件长度
        long tempFileLength = randomFile.length();
        randomFile.close();
        return tempFileLength;
    }

    //发送文件的接收情况
    private void sendRecivedLength(Request request, long tempFileLength, Socket socket) throws IOException {
        Request request1 = new Request();
        request1.setSocketType(Constant.SocketType.RECIVE_SENDED_FILE_LENGTH_AND_SEND_FILE.getType());
        if (tempFileLength < getFileTotalLength(request)) {
            //文件未传输完成
            //向发送方发送已接收的文件长度
            FileAttribute fileAttribute = inCompleteFileMap.get(request.getUsername()).get(request.getFileName());
            request1.setSrcPath(fileAttribute.getPath());
            request1.setRecivedFileLength(tempFileLength);
            request1.setFileName(request.getFileName());
            request1.setCompleted(false);
        } else {
            request1.setCompleted(true);
        }
        String requestJson = JSON.toJSONString(request1);
        sendMessage(requestJson, socket);
    }
}
