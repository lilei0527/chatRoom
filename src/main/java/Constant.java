/**
 * @author lilei
 * create at 2019/10/21 13:41
 */
class Constant {
    //包的实际长度所占的字节数  单位个
    static final int PER_PACAGE_LENGTH = 4; //每次发送用四个字节来表示发送的包的字节长度

    //传输文件用到的缓冲区的大小
    static final int FILE_BUFFER_SIZE = 1024 * 8;

    //移动文件用的缓冲区的大小
    static final int MOVE_FILE_BUFFER_SIZE = 4*1024*1024;//4M



    //服务器端口
    static final int PORT = 6666;

    //服务器地址
    static final String ADDRESS = "localhost";

    //客户端和服务端通用的socket类型
    public enum SocketType {

        RECIVE_FILE("reciveFile"),

        RECIVE_SENDED_FILE_LENGTH_AND_SEND_FILE("reciveSendedFileLengthAndSendFile");


        private final String type;

        SocketType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
