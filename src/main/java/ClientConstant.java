/**
 * @author lilei
 * create at 2019/10/22 10:06
 */
class ClientConstant {

    //客户端文件保存位置
    static String FILE_SAVE_PALCE = "D:/file";
    //临时文件的保存位置
    static String TEMP_FILE_SAVE_PALCE = "D:/tempfile";

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
