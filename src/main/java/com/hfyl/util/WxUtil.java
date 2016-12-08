package com.hfyl.util;

import java.security.MessageDigest;
import java.util.UUID;

/**
 * Created by xyj on 2016/12/8.
 */
public class WxUtil {

    /**
     *  生成随机UUID
     * @return
     */
    public static  String getnoncestr(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }

        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes());

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }

}
