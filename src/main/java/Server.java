import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends ChatContext {

    private ServerSocket serverSocket = new ServerSocket(Constant.PORT);
    //存储所有连接用户的socket
    private Map<String, Socket> stringSocketMap = new HashMap<>();
    //存储用户的离线消息<接受者名字，消息集合>
    private Map<String, List<String>> offLineMessageMap = new HashMap<>();
    //存储用户的离线文件 <接受者名字，<文件名，过期时间>>
    private Map<String, Map<String, Date>> offLineFileMap = new HashMap<>();
    //用户的用户名密码
    private Map<String, String> registerMap = new HashMap<>();


    private Server() throws IOException {
        loadRegisterInfo();
        sendMessageToOfflineRecoveOnline();
//        sendFileToOfflineRecoveOnline();
        deleteExpireFile();
        while (true) {
            try {
                init();
            } catch (IOException e) {
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }


    //初始聊天服务器
    public void init() throws IOException {

        Socket socket = serverSocket.accept();
        System.out.println(socket.getRemoteSocketAddress() + "已连接");
//        String name = "people" + PEOPLESUM;
//        stringSocketMap.put(name, socket);
        reciveSocket(socket);
    }

    //处理客户端请求
    private void reciveSocket(Socket socket) {
        Runnable runnable = () -> {
            while (true) {
                try {
                    List<String> list = reciveMessage(socket);
                    handleClientRequest(list, socket);
                } catch (IOException e) {
                    System.out.println("IO异常");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        threadPool.submit(runnable);
    }

    //对不同的客户端消息做出不同的响应
    private void handleClientRequest(List<String> requestString, Socket socket) throws Exception {
        for (String aRequestString : requestString) {
            if (aRequestString == null) {
                break;
            }
            System.out.println("提取的消息为:" + aRequestString);
            Request request = JSON.parseObject(aRequestString, Request.class);
            if (ServerConstant.ServerSocketType.CHAT_TO_ALL.getType().equals(request.getSocketType())) {
                System.out.println("群聊");
                handleAllChatRequest(request);
            }

            if (ServerConstant.ServerSocketType.CHAT_TO_ONE.getType().equals(request.getSocketType())) {
                handleSingleChatRequest(request);
            }

            if (ServerConstant.ServerSocketType.COLSE.getType().equals(request.getSocketType())) {
                handleCloseRequest(socket);
            }

//            if (ServerConstant.ServerSocketType.SEND_FILE.getType().equals(request.getSocketType())) {
//                handleSendFile(request);
//            }

            if (Constant.SocketType.RECIVE_FILE.getType().equals(request.getSocketType())) {
                handleReciveFile(request, ServerConstant.TEMP_FILE_SAVE_PALCE, ServerConstant.OFFLINE_FILE_SAVE_PALCE, ServerConstant.IN_COMPLETE_FILE_MAP_SAVE_PALCE, socket);
            }

            if (ServerConstant.ServerSocketType.LOGIN.getType().equals(request.getSocketType())) {
                handleLoginRequest(request, socket);
            }
        }
    }


    //处理群聊请求
    private void handleAllChatRequest(Request request) {
        System.out.println(request.getUsername() + ":" + request.getMessage());
    }


    //处理私聊请求
    private void handleSingleChatRequest(Request request) {
        try {
            //封装消息类型
            Request requestToClient = new Request();
            requestToClient.setMessage(request.getMessage());
            requestToClient.setUsername(request.getUsername());
            requestToClient.setSocketType(ClientConstant.ClientSocketType.CHAT_WITH_CLIENT.getType());
            String requestJson = JSONObject.toJSONString(requestToClient);
            handleMessage(requestJson, request.getName());
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }


    //客户端退出，处理关闭socket的请求
    private void handleCloseRequest(Socket socket) throws IOException {
        close(socket);
    }

    //给客户端发送服务器生成的唯一名字
    private void sendNameToClient(String name, Socket socket) throws IOException {
        //封装消息类型
        Request request = new Request();
        request.setMessage(name);
        request.setLogin(true);
        request.setSocketType(ClientConstant.ClientSocketType.GIVE_NAME.getType());
        String requestJson = JSONObject.toJSONString(request);
        sendMessage(requestJson, socket);
    }


    //给上线的人发送消息
    private void sendMessageToOfflineRecoveOnline() {
        TimerTask task = new TimerTask() { //创建一个新的timer task
            public void run() { //定时器任务执行的操作
                for (Map.Entry<String, List<String>> offLineMessageEntry : offLineMessageMap.entrySet()) {
                    for (Map.Entry<String, Socket> stringSocketEntry : stringSocketMap.entrySet()) {
                        if (offLineMessageEntry.getKey().equals(stringSocketEntry.getKey())) {
                            for (String message : offLineMessageEntry.getValue()) {
                                try {
                                    sendMessage(message, stringSocketEntry.getValue());
                                } catch (IOException e) {
                                    System.out.println("io exception");
                                    return;
                                }
                            }
                            offLineMessageMap.remove(offLineMessageEntry.getKey());
                        }
                    }
                }
            }
        };
        Timer timer = new Timer();//创建一个定时器
        long delay = 0;
        long PeriodTime = ServerConstant.CHECK_ONLINE_TIME_PERIOD;
        timer.scheduleAtFixedRate(task, delay, PeriodTime);
    }

//    //给上线的人发送文件
//    private void sendFileToOfflineRecoveOnline() {
//        TimerTask task = new TimerTask() { //创建一个新的timer task
//            public void run() { //定时器任务执行的操作
//                for (Map.Entry<String, Map<String, Date>> offLineFileEntry : offLineFileMap.entrySet()) {
//                    for (Map.Entry<String, Socket> stringSocketEntry : stringSocketMap.entrySet()) {
//                        if (offLineFileEntry.getKey().equals(stringSocketEntry.getKey())) {
//                            //如果用户上线
//                            for (Map.Entry<String, Date> fileEntry : offLineFileEntry.getValue().entrySet()) {
//                                //发送所有的离线文件给上线用户
//                                try {
//                                    File file = new File(ServerConstant.OFFLINE_FILE_SAVE_PALCE + "/" + fileEntry.getKey());
//                                    if (!Util.isFileExpire(fileEntry.getValue())) {
//                                        sendFile(file, "", stringSocketEntry.getValue(), Constant.SocketType.RECIVE_FILE.getType());
//                                    } else {
//                                        System.out.println("文件已过期");
//                                    }
//                                } catch (IOException e) {
//                                    System.out.println("IOException");
//                                }
//                            }
//                            //发送完毕后离线文件map去掉这个文件
//                            offLineFileMap.remove(offLineFileEntry.getKey());
//                        }
//                    }
//                }
//            }
//        };
//        Timer timer = new Timer();//创建一个定时器
//        long delay = 0;
//        long PeriodTime = ServerConstant.CHECK_ONLINE_TIME_PERIOD;
//        timer.scheduleAtFixedRate(task, delay, PeriodTime);
//    }

    //给在线的人发送消息 name发送的人的名称
    private boolean sendRequestToOnline(String name, String requestString) throws IOException {
        boolean isOnline = false;
        for (Map.Entry<String, Socket> entry : stringSocketMap.entrySet()) {
            if (name.equals(entry.getKey())) {
                //用户在线直接发送消息
                sendMessage(requestString, entry.getValue());
                isOnline = true;
                break;
            }
        }
        return isOnline;
    }

    //将离线文字消息添加到map，name是接收人的名字
    private void handleMessage(String message, String name) throws IOException {
        boolean isOnline = sendRequestToOnline(name, message);
        //如果用户不在线
        if (!isOnline) {
            List<String> messageList = new ArrayList<>();
            messageList.add(message);
            //存储离线消息
            if (offLineMessageMap.isEmpty()) {
                offLineMessageMap.put(name, messageList);
            } else {
                //用来标识离线消息map中是否有本次消息接受者的信息
                boolean hasMessage = false;
                for (Map.Entry<String, List<String>> offLineMessageEntry : offLineMessageMap.entrySet()) {
                    //如果离线消息的map中已经有该未上线人的消息，则将消息添加到消息集合
                    if (offLineMessageEntry.getKey().equals(name)) {
                        List<String> messageListExist = offLineMessageEntry.getValue();
                        messageListExist.addAll(messageList);
                        offLineMessageEntry.setValue(messageListExist);
                        hasMessage = true;
                        break;
                    }
                }
                if (!hasMessage) {
                    offLineMessageMap.put(name, messageList);
                }
            }
        }
    }


    //将离线文件消息存储到文件，name是接收人的名字
//    private void handleFile(Request request) throws IOException {
//        System.out.println(stringSocketMap.toString());
//        System.out.println("消息：" + request.toString());
//        String name = request.getName();
//        String message = JSON.toJSONString(request);
//        boolean isOnline = sendRequestToOnline(name, message);
//        System.out.println(isOnline);
//        if (!isOnline) {
//            System.out.println("用户离线，保存文件");
//            //保存离线文件
//            handleReciveFile(request, ServerConstant.TEMP_FILE_SAVE_PALCE, ServerConstant.OFFLINE_FILE_SAVE_PALCE);
//            //已存在的离线文件
//            Map<String, Date> exsitFileMap = offLineFileMap.get(name);
//            //将文件名添加到map
//            Map<String, Date> fileMap = new HashMap<>();
//            fileMap.put(request.getFileName(), Util.getFileExpireTime());
//            //将现在的离线文件和之前的离线文件添加到一起
//            if (exsitFileMap != null) {
//                fileMap.putAll(exsitFileMap);
//            }
//            //存储离线消息
//            offLineFileMap.put(name, fileMap);
//            System.out.println(offLineFileMap.toString());
//        }
//    }

    //定期删除过期的用户离线文件
    private void deleteExpireFile() {
        TimerTask task = new TimerTask() { //创建一个新的timer task
            public void run() { //定时器任务执行的操作
                System.out.println("检查有无过期的消息");
                Iterator<Map.Entry<String, Map<String, Date>>> offLineFileIterator = offLineFileMap.entrySet().iterator();
                while (offLineFileIterator.hasNext()) {
                    Map.Entry<String, Map<String, Date>> offLineFileEntry = offLineFileIterator.next();
                    System.out.println(offLineFileEntry.toString());
                    Iterator<Map.Entry<String, Date>> fileIterator = offLineFileEntry.getValue().entrySet().iterator();
                    while (fileIterator.hasNext()) {
                        Map.Entry<String, Date> fileEntry = fileIterator.next();
                        System.out.println(fileEntry.toString());
                        if (Util.isFileExpire(fileEntry.getValue())) {
                            File file = new File(ServerConstant.OFFLINE_FILE_SAVE_PALCE + "/" + fileEntry.getKey());
                            if (file.exists()) {
                                if (file.delete()) {
                                    //移除内存中文件的引用
                                    fileIterator.remove();
                                    System.out.println("移除" + fileEntry.getKey());
                                } else {
                                    System.out.println(file.getName() + "文件删除失败");
                                }
                            }
                        }
                    }

                    if (offLineFileEntry.getValue().size() == 0) {
                        //所有文件都发送完毕
                        offLineFileIterator.remove();
                        System.out.println("移除" + offLineFileEntry.getKey());
                    }
                }
            }
        };
        Timer timer = new Timer();//创建一个定时器
        long delay = 0;
        long PeriodTime = ServerConstant.DELETE_EXPIRE_FILE_PERIOD;
        timer.scheduleAtFixedRate(task, delay, PeriodTime);
    }

    long handleReciveFile(Request request, String tempSavePosition, String savePosition, String inCompleteFileMapSavePosition, Socket socket) throws Exception {
        long tempFileLength = super.handleReciveFile(request, tempSavePosition, savePosition, inCompleteFileMapSavePosition, socket);

        if (tempFileLength == getFileTotalLength(request)) {
            File tempFile = new File(tempSavePosition + "/" + request.getFileName());
            File saveFile = new File(savePosition + "/" + request.getFileName());
            copyFile(tempFile, saveFile);
            boolean delete = tempFile.delete();
            if (!delete) {
                throw new Exception();
            }
        }
        return tempFileLength;
    }

    //处理注册请求
    private void handleRegisterRequest(Request request, Socket socket) {
        String username = request.getUsername();
        String password = request.getPassword();

    }


    //加载用户注册文件
    private void loadRegisterInfo() throws IOException {
        InputStream inputStream = new FileInputStream(ServerConstant.REGISTER_INFO_SAVE_PLACE + ServerConstant.REGISTER_INFO_NAME);
        StringBuilder stringBuilder = new StringBuilder();
        byte[] bytes = new byte[Constant.LOAD_REGISTER_INFO_BUFFER_SIZE];
        int c;
        while ((c = inputStream.read(bytes)) != -1) {
            byte[] shorBytes = Util.shortByteArray(bytes, c);
            String s = new String(shorBytes);
            stringBuilder.append(s);
        }
        String registerJson = stringBuilder.toString();
        Map map = JSON.parseObject(registerJson, Map.class);
        registerMap.putAll(map);
    }

    //处理登录请求
    private void handleLoginRequest(Request request, Socket socket) throws IOException {
        System.out.println(request);
        boolean isLogin = false;
        System.out.println(registerMap);
        for (Map.Entry<String, String> registerEntry : registerMap.entrySet()) {
            System.out.println(registerEntry.getKey()+","+registerEntry.getValue());
            System.out.println(request.getUsername()+","+request.getPassword());
            if (registerEntry.getKey().equals(request.getUsername()) && registerEntry.getValue().equals(request.getPassword())) {
                //登录成功
                sendNameToClient(request.getUsername(), socket);
                stringSocketMap.put(request.getUsername(), socket);
                isLogin = true;
                break;
            }
        }
        if(!isLogin){
            Request request1 = new Request();
            request1.setLogin(false);
            request1.setSocketType(ClientConstant.ClientSocketType.GIVE_NAME.getType());
            String requestJson = JSONObject.toJSONString(request1);
            sendMessage(requestJson, socket);
        }
    }
}

