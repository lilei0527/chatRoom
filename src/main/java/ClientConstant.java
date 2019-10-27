/**
 * @author lilei
 * create at 2019/10/22 10:06
 */
class ClientConstant {
    private static final String ROOT_PATH = "/client";
    //客户端文件保存位置
    static String FILE_SAVE_PALCE = Constant.ROOT_PATH+ClientConstant.ROOT_PATH+"/file";
    //临时文件的保存位置
    static String TEMP_FILE_SAVE_PALCE = Constant.ROOT_PATH+ClientConstant.ROOT_PATH+"/tempfile";
    //未完成文件map的保存位置
    static String  IN_COMPLETE_FILE_MAP_SAVE_PALCE = Constant.ROOT_PATH+ClientConstant.ROOT_PATH+"/incompletemapfile";
    //未完成文件的文件名
    static String IN_COMPLETE_FILE_MAP_NAME = "incomplete_file.txt";
    //客户端接收的socket类型
    public enum ClientSocketType {

        CHAT_WITH_CLIENT("chatWithClient"),

        GIVE_NAME("giveName");



        private final String type;

        ClientSocketType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
