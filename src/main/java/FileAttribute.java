import java.util.Date;

/**
 * @author lilei
 * create at 2019/10/24 14:36
 */
public class FileAttribute {
    //文件过期时间
    private Date expireDate;

    //文件的存储路径
    private String path;

    //文件的总长度
    private long TotalLength;

    //文件是否接收完成的标记
    private boolean isCompleted;

    //接收人的标识(发送给谁)
    private String reciveName;



    //文件名
    private String fileName;

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTotalLength() {
        return TotalLength;
    }

    public void setTotalLength(long totalLength) {
        TotalLength = totalLength;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getReciveName() {
        return reciveName;
    }

    public void setReciveName(String reciveName) {
        this.reciveName = reciveName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "FileAttribute{" +
                "expireDate=" + expireDate +
                ", path='" + path + '\'' +
                ", TotalLength=" + TotalLength +
                ", isCompleted=" + isCompleted +
                ", reciveName='" + reciveName + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
