/**
 * @author lilei
 * create at 2019/10/22 10:06
 */
class ServerConstant {

    //文件的过期时间
//    static final int FILE_EXPIRE_TIME = 10*60*1000; //10分钟
    static final int FILE_EXPIRE_TIME = 20*1000; //20秒

    //发送离线文件时，检查用户是否在线的间隔
    static final int CHECK_ONLINE_TIME_PERIOD = 10*1000; //十秒

    //删除过期离线文件的时间间隔
    static final int DELETE_EXPIRE_FILE_PERIOD = 10*1000;//十秒

    //离线文件的存储位置
    static String OFFLINE_FILE_SAVE_PALCE = "D:/offlinefile";

    //服务器接收的socket类型
    public enum ServerSocketType {
        //关闭socket
        COLSE("close"),

        //单聊
        CHAT_TO_ONE("chatToOne"),

        //群聊
        CHAT_TO_ALL("chatToAll"),

        //发送文件
        SEND_FILE("sendFile"),

        //接收文件
        RECIVE_FILE("reciveFile");

        private final String Type;

        ServerSocketType(String Type) {
            this.Type = Type;
        }

        public String getType() {
            return Type;
        }
    }
}
