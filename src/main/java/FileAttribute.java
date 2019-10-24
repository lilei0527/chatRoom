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

    //文件的长度
    private long length;






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

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
