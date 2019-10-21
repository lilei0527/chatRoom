import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server extends ChatContext {

    private ServerSocket serverSocket = new ServerSocket(Constant.PORT);

    private Map<String, Socket> stringSocketMap = new HashMap<>();

    private int PEOPLESUM;

    private Server() throws IOException {
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
    private void handleClientRequest(List<String>requestString, Socket socket) throws IOException {
        for (String aRequestString : requestString) {
            if (aRequestString == null) {
                break;
            }
            System.out.println("提取的消息为:" + aRequestString);
            Request request = JSON.parseObject(aRequestString, Request.class);
            System.out.println(request);
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
            for (Map.Entry<String, Socket> entry : stringSocketMap.entrySet()) {
                if (request.getName().equals(entry.getKey())) {
                    //封装消息类型
                    Request requestToClient = new Request();
                    requestToClient.setMessage(request.getMessage());
                    requestToClient.setSendName(request.getSendName());
                    requestToClient.setSocketType(Constant.ClientSocketType.CHAT_WITH_CLIENT.getType());

                    String requestJson = JSONObject.toJSONString(requestToClient);
                    sendMessage(requestJson, entry.getValue());
                }
            }
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
    private void handleSendFile(Request request) {
        try {
            for (Map.Entry<String, Socket> entry : stringSocketMap.entrySet()) {
                if (request.getName().equals(entry.getKey())) {
                    //封装消息类型
                    Request requestToClient = new Request();
                    requestToClient.setBytes(request.getBytes());
                    requestToClient.setSendName(request.getSendName());
                    requestToClient.setSocketType(Constant.ClientSocketType.RECIVE_FILE.getType());
                    requestToClient.setFileName(request.getFileName());

                    String requestJson = JSONObject.toJSONString(requestToClient);
                    System.out.println("发送的文件：" + requestJson);
                    sendMessage(requestJson, entry.getValue());
                }
            }
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

}
