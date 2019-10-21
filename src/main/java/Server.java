import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends ChatContext {

    private ServerSocket serverSocket = new ServerSocket(Constant.PORT);
    //存储所有连接用户的socket
    private Map<String, Socket> stringSocketMap = new HashMap<>();
    //存储用户的离线消息
    private Map<String, List<String>> offLineMessageMap = new HashMap<>();
    //用户人数
    private int PEOPLESUM;

    private Server() throws IOException {
        sendMessageToOffline();
        while (true) {
            try {
                init();
                PEOPLESUM++;
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
        String name = "people" + PEOPLESUM;
        stringSocketMap.put(name, socket);
        sendNameToClient(name, socket);
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
                }
            }
        };
        threadPool.submit(runnable);
    }

    //对不同的客户端消息做出不同的响应
    private void handleClientRequest(List<String> requestString, Socket socket) throws IOException {
        for (String aRequestString : requestString) {
            if (aRequestString == null) {
                break;
            }
            System.out.println("提取的消息为:" + aRequestString);
            Request request = JSON.parseObject(aRequestString, Request.class);
            if (Constant.ServerSocketType.CHAT_TO_ALL.getType().equals(request.getSocketType())) {
                System.out.println("群聊");
                handleAllChatRequest(request);
            }

            if (Constant.ServerSocketType.CHAT_TO_ONE.getType().equals(request.getSocketType())) {
                handleSingleChatRequest(request);
            }

            if (Constant.ServerSocketType.COLSE.getType().equals(request.getSocketType())) {
                handleCloseRequest(socket);
            }

            if (Constant.ServerSocketType.SEND_FILE.getType().equals(request.getSocketType())) {
                handleSendFile(request);
            }
        }
    }

    //处理群聊请求
    private void handleAllChatRequest(Request request) {
        System.out.println(request.getSendName() + ":" + request.getMessage());
    }


    //处理私聊请求
    private void handleSingleChatRequest(Request request) {
        try {
            //封装消息类型
            Request requestToClient = new Request();
            requestToClient.setMessage(request.getMessage());
            requestToClient.setSendName(request.getSendName());
            requestToClient.setSocketType(Constant.ClientSocketType.CHAT_WITH_CLIENT.getType());
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
        request.setSocketType(Constant.ClientSocketType.GIVE_NAME.getType());
        String requestJson = JSONObject.toJSONString(request);
        sendMessage(requestJson, socket);
    }

    //发送文件
    private void handleSendFile(Request request) throws IOException {
        //封装消息类型
        Request requestToClient = new Request();
        requestToClient.setBytes(request.getBytes());
        requestToClient.setSendName(request.getSendName());
        requestToClient.setSocketType(Constant.ClientSocketType.RECIVE_FILE.getType());
        requestToClient.setFileName(request.getFileName());
        String requestJson = JSONObject.toJSONString(requestToClient);
        handleMessage(requestJson,request.getName());
    }

    //给离线的人发送消息
    private void sendMessageToOffline() {
        Runnable runnable = () -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println(offLineMessageMap.toString() + stringSocketMap.toString());
                for (Map.Entry<String, List<String>> offLineMessageEntry : offLineMessageMap.entrySet()) {
                    for (Map.Entry<String, Socket> stringSocketEntry : stringSocketMap.entrySet()) {
                        if (offLineMessageEntry.getKey().equals(stringSocketEntry.getKey())) {
                            System.out.println(offLineMessageEntry.getValue().size());
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
        threadPool.submit(runnable);
    }

    //将离线消息（文件或文字）添加到map，name是接收人的名字
    private void handleMessage(String message, String name) throws IOException {
        for (Map.Entry<String, Socket> entry : stringSocketMap.entrySet()) {
            if (name.equals(entry.getKey())) {
                sendMessage(message, entry.getValue());
                break;
            } else {
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
    }
}
