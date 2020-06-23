package com.aliyun.alink.devicesdk.manager;

import android.content.Context;
import android.widget.Toast;

import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.linksdk.tools.ThreadTools;

public class ToastUtils {

    public static void showToast(String message) {
        showToast(message, DemoApplication.getAppContext());
    }

    private static void showToast(final String message, final Context context) {
        ThreadTools.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
