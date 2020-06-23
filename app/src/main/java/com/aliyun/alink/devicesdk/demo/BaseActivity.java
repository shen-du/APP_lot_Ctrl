package com.aliyun.alink.devicesdk.demo;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.alink.devicesdk.manager.ToastUtils;
import com.aliyun.alink.linksdk.tools.ALog;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Copyright (c) 2014-2016 Alibaba Group. All rights reserved.
 * License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

public class BaseActivity extends Activity {
    private static final String TAG = "BaseActivity";
    protected static SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    protected String logStr = null;
    protected TextView textView = null;

    public static void showToast(final String message){
        ALog.d(TAG, "showToast() called with: message = [" + message + "]");
        ToastUtils.showToast(message);
    }

    @Override
    protected void onPause() {
        super.onPause();
        logStr = null;
    }

    public void log(final String tag, final String message){
        if (textView == null){
            textView = findViewById(R.id.textview_console);
        }
        if (textView == null) {
            return;
        }
        try {
            textView.post(new Runnable() {
                @Override
                public void run() {
                    if (textView != null){
                        logStr += message + "\n";
                        textView.setText(logStr);
                    } else {
                        Log.d(TAG, "textview=null");
                    }
                    Log.d(TAG, message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getTime(){
        return fm.format(new Date());
    }

    protected void showLoading(){

    }

    protected void hideLoading(){

    }
}
