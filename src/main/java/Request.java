import java.util.Arrays;

class Request {
    //请求的类型
    private String socketType;
    //接受信息的人的名字
    private String name;
    //要发送的信息
    private String message;

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
    //文件是否传输完成
    private boolean isCompleted;
    //用户名
    private String username;
    //密码
    private String password;
    //是否登录成功
    private boolean isLogin;



    Request() {

    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
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
                ", bytes=" + Arrays.toString(bytes) +
                ", fileName='" + fileName + '\'' +
                ", srcPath='" + srcPath + '\'' +
                ", totalFileLength=" + totalFileLength +
                ", recivedFileLength=" + recivedFileLength +
                ", isCompleted=" + isCompleted +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", isLogin=" + isLogin +
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



}
