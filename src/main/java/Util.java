import java.io.File;
import java.util.Date;

/**
 * @author lilei
 * create at 2019/10/21 13:44
 */

//int转btye[]
class Util {
    static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    //btye[]转int
    static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }


    //获取字符串的字节长度
    static int getStringByteLength(String str) {
        int realLength = 0;
        for (int i = 0; i < str.length(); i++) {
            char charCode = str.charAt(i);
            if (charCode <= 128)
                realLength += 1;
            else
                realLength += 2;
        }
        return realLength;
    }

    //将bytes数组缩小为指定大小的数组
    static byte[] shortByteArray(byte[] bytes, int length) {
        if (bytes.length < length) {
            throw new IllegalArgumentException();
        }
        byte[] newBytes = new byte[length];
        System.arraycopy(bytes, 0, newBytes, 0, length);
        return newBytes;
    }

    //取得文件的过期时间
    static Date getFileExpireTime() {
        long expireTime = System.currentTimeMillis() + ServerConstant.FILE_EXPIRE_TIME;
        return new Date(expireTime);
    }

    //判断文件是否过期
    static Boolean isFileExpire(Date expireDate){
        Date now = new Date();
        return now.after(expireDate);
    }


}
