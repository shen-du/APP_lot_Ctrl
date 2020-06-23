package com.aliyun.alink.devicesdk.manager;

import android.text.TextUtils;

import com.aliyun.alink.linksdk.tools.ALog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Util {
    private static final String TAG = "MD5Util";

    public static String getFileMd5(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            ALog.e(TAG, "getMd5ByFile filepath=null.");
            return null;
        }

        File file = null;
        try {
            file = new File(filepath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (file == null) {
            ALog.e(TAG, "getMd5ByFile file not exist.");
            return null;
        }
        InputStream inputStream = null;
        MessageDigest md5 = null;

        byte[] buffer = new byte[1024 * 8];
        int readCount = 0;

        try {
            inputStream = new FileInputStream(file);
            md5 = MessageDigest.getInstance("MD5");
            while ((readCount = inputStream.read(buffer)) > 0) {
                md5.update(buffer, 0, readCount);
            }
            return hexString(md5.digest());
        } catch (Exception e) {
            System.out.println("error");
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String hexString(byte[] md5Bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }

}