import java.util.Arrays;

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
    //文件的源路径
    private String srcPath;
    //文件总种长度
    private long totalFileLength;
    //已接收的文件长度
    private long recivedFileLength;


    Request() {

    }

    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public long getTotalFileLength() {
        return totalFileLength;
    }

    public void setTotalFileLength(long totalFileLength) {
        this.totalFileLength = totalFileLength;
    }

    @Override
    public String toString() {
        return "Request{" +
                "socketType='" + socketType + '\'' +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                ", sendName='" + sendName + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                ", fileName='" + fileName + '\'' +
                ", totalFileLength=" + totalFileLength +
                ", recivedFileLength=" + recivedFileLength +
                '}';
    }

    public long getRecivedFileLength() {
        return recivedFileLength;
    }

    public void setRecivedFileLength(long recivedFileLength) {
        this.recivedFileLength = recivedFileLength;
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
