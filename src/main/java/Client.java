import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

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
        Socket socket = new Socket(Constant.ADDRESS, Constant.PORT);
        //接受服务器消息
        reciveSocket(socket);
        File file = new File("D:/1.jpg");
//        File file1 = new File("D:/1.txt");
//        File file2 = new File("D:/Postman-win64-7.0.6-Setup.exe");
//        发送文件
        sendRandomFile(file,0,socket);
//        sendFile(file, "people1", socket,ServerConstant.ServerSocketType.SEND_FILE.getType());
//        sendFile(file1, "people1", socket,ServerConstant.ServerSocketType.SEND_FILE.getType());
//        sendFile(file2,"people0",socket);
//        sendMessageToOne(socket,"people1");
//
//        sendMessageToAll(socket);

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
                request.setSocketType(ServerConstant.ServerSocketType.CHAT_TO_ONE.getType());
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
    private void sendMessageToAll(Socket socket) {
        Runnable runnable1 = () -> {
            while (true) {
                String message = getKeyboardEntry(socket.getRemoteSocketAddress().toString());

                //封装消息类型
                Request request = new Request();
                request.setMessage(message);
                request.setSendName(name);
                request.setSocketType(ServerConstant.ServerSocketType.CHAT_TO_ALL.getType());
                String requestJson = JSON.toJSONString(request);


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


    //接受服务器请求
    private void reciveSocket(Socket socket) {
        Runnable runnable = () -> {
            while (true) {
                try {
                    List<String> list = reciveMessage(socket);
                    handleServerRequest(list,socket);
                    System.out.println("处理请求数:"+list.size());
                } catch (IOException e) {
                    return;
                }
            }
        };
        threadPool.submit(runnable);
    }

    //对不同的服务端消息做出不同的响应
    private void handleServerRequest(List<String> requestString,Socket socket) throws IOException {
        for (String aRequestString : requestString) {
            if (aRequestString == null) {
                break;
            }
            System.out.println("提取的消息为:"+aRequestString);
            Request request = JSON.parseObject(aRequestString, Request.class);
            if (ClientConstant.ClientSocketType.CHAT_WITH_CLIENT.getType().equals(request.getSocketType())) {
                handleChatWithClientRequest(request);
            }

            if (ClientConstant.ClientSocketType.GIVE_NAME.getType().equals(request.getSocketType())) {
                handleGiveNameRequest(request);
            }

//            if (Constant.SocketType.RECIVE_FILE.getType().equals(request.getSocketType())) {
//                handleReciveFile(request,ClientConstant.TEMP_FILE_SAVE_PALCE,ClientConstant.FILE_SAVE_PALCE,socket);
//            }

            if(Constant.SocketType.RECIVE_SENDED_FILE_LENGTH_AND_SEND_FILE.getType().equals(request.getSocketType())){
                reciveAndSendRandomFile(socket,request);
            }
        }

    }

    //处理服务器转发的客户端聊天请求
    private void handleChatWithClientRequest(Request request) {
        System.out.println(request.getSendName() + ":" + request.getMessage());
    }

    //接受服务器的命名
    private void handleGiveNameRequest(Request request) {
        name = request.getMessage();
    }


}
