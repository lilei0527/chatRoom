import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.Socket;

public class Client extends ChatContext {
    //由服务器生成的客户端的名字
    private String name;


    public static void main(String[] args) throws IOException {
        new Client();
    }

    private Client() throws IOException {
        init();
    }


    @Override
    public void init() throws IOException {


        Socket socket = new Socket(ADDRESS, PORT);

        //接受服务器消息
        reciveSocket(socket);

//        sendMessageToOne(socket,"people0");

        sendMessageToAll(socket);

    }

    //私聊发送消息
    private void sendMessageToOne(Socket socket, String sendToname) {
        Runnable runnable1 = () -> {
            while (true) {
                String message = getKeyboardEntry(sendToname);

                //封装消息类型
                Request request = new Request();
                request.setMessage(message);
                request.setName(sendToname);
                request.setSendName(name);
                request.setSocketType(ServerSocketType.CHAT_TO_ONE.getType());
                String requestJson = JSONObject.toJSONString(request);

                try {
                    sendMessage(requestJson, socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        threadPool.submit(runnable1);
    }

    //群聊发送消息
    private void sendMessageToAll(Socket socket){
        Runnable runnable1 = () -> {
            while (true) {
                String message = getKeyboardEntry(socket.getRemoteSocketAddress().toString());

                //封装消息类型
                Request request = new Request();
                request.setMessage(message);
                request.setSendName(name);
                request.setSocketType(ServerSocketType.CHAT_TO_ALL.getType());
                String requestJson = JSON.toJSONString(request);

                try {
                    sendMessage(requestJson,socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        threadPool.submit(runnable1);
    }



    //接受服务器请求
    private void reciveSocket(Socket socket) {
        Runnable runnable = () -> {
            while (true) {
                String s;
                try {
                    s = reciveMessage(socket);
                    handleServerRequest(s);
                } catch (IOException e) {
                    return;
                }
            }
        };
        threadPool.submit(runnable);
    }

    //对不同的服务端消息做出不同的响应
    private void handleServerRequest(String requestString) throws IOException {
        Request request = JSON.parseObject(requestString, Request.class);
        if (ClientSocketType.CHAT_WITH_CLIENT.getType().equals(request.getSocketType())) {
            handleChatWithClientRequest(request);
        }

        if (ClientSocketType.GIVE_NAME.getType().equals(request.getSocketType())) {
            handleGiveNameRequest(request);
        }

    }

    //处理服务器转发的客户端聊天请求
    private void handleChatWithClientRequest(Request request){
        System.out.println(request.getSendName() + ":" + request.getMessage());
    }

    //接受服务器的命名
    private void handleGiveNameRequest(Request request){
        name = request.getMessage();
    }
}
