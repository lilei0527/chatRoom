class Request {
    //请求的类型
     private String socketType;
    //接受信息的人的名字
    private String name;
    //要发送的信息
    private String message;
    //发送信息的人的名字
    private String sendName;
    //发送的二进制流
    private byte[] bytes;
    //文件名
    private String fileName;
    //一个包的最大长度 单位为字节
    public static int MAX_PACAGE_SIZE =  1000;
    //包的实际长度所占的字节数  单位个
    public static int PER_PACAGE_LENGTH = 4; //每次发送用四个字节来表示发送的包的字节长度


    Request() {

    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getSocketType() {
        return socketType;
    }

    public void setSocketType(String socketType) {
        this.socketType = socketType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSendName() {
        return sendName;
    }

    public void setSendName(String sendName) {
        this.sendName = sendName;
    }
}
