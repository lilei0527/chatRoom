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

    private static final String ROOT_PATH = "/server";

    //离线文件的存储位置
    static String OFFLINE_FILE_SAVE_PALCE = Constant.ROOT_PATH+ServerConstant.ROOT_PATH+"/offlinefile";

    //临时离线文件的保存位置
    static String TEMP_FILE_SAVE_PALCE = Constant.ROOT_PATH+ServerConstant.ROOT_PATH+"/tempfile";

    //未完成文件map的保存位置
    static String  IN_COMPLETE_FILE_MAP_SAVE_PALCE = Constant.ROOT_PATH+ServerConstant.ROOT_PATH+"/incompletemapfile";

    //未完成文件的文件名
    static String IN_COMPLETE_FILE_MAP_NAME = "inComplete_file.txt";

    //保存注册信息的文件位置
    static String REGISTER_INFO_SAVE_PLACE = Constant.ROOT_PATH+ServerConstant.ROOT_PATH+"/register";

    //保存注册信息的文件名
    static String REGISTER_INFO_NAME = "/register.txt";



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

        //注册
        REGISTER("register"),

        //登录
        LOGIN("login");


        private final String Type;

        ServerSocketType(String Type) {
            this.Type = Type;
        }

        public String getType() {
            return Type;
        }
    }
}
