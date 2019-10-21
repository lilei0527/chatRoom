/**
 * @author lilei
 * create at 2019/10/21 13:41
 */
class Constant {
    //包的实际长度所占的字节数  单位个
    static final int PER_PACAGE_LENGTH = 4; //每次发送用四个字节来表示发送的包的字节长度

    //读取文件用到的缓冲区的大小
    static final int FILE_BUFFER_SIZE = 1024 * 8;

    //服务器端口
    static int PORT = 6666;

    //服务器地址
    static String ADDRESS = "localhost";

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
}
