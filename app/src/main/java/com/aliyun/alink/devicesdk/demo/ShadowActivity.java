package com.aliyun.alink.devicesdk.demo;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.api.IShadowRRPC;
import com.aliyun.alink.dm.shadow.ShadowResponse;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tmp.device.payload.cloud.ResponsePayload;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ThreadTools;

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

public class ShadowActivity extends BaseTemplateActivity {
    private static final String TAG = "ShadowActivity";

    private static long version = 1;

    private static String shadowUpdate = "{" + "\"method\": \"update\"," + "\"state\": {" + "\"reported\": {" +
            "\"color\": \"red\"" + "}" + "}," + "\"version\": {ver}" + "}";

    private String shadowGet = "{" + "\"method\": \"get\"" + "}";

    private String shadowDelete = "{" + "\"method\": \"delete\"," + "\"state\": {" + "\"reported\": {" +
            "\"color\": \"null\"" + "}" + "}," + "\"version\": {ver}" + "}";

    private String shadowDeleteAll = "{\n" +
            "\"method\": \"delete\",\n" +
            "\"state\": {\n" +
            "\"reported\":\"null\"\n" +
            "},\n" +
            "\"version\": {ver}\n" +
            "}";

    @Override
    protected void initViewData() {
        funcTV1.setText("更新设备影子");
        funcET1.setText(shadowUpdate);
        funcBT1.setText("更新");

        funcTV2.setText("获取设备影子");
        funcET2.setText(shadowGet);
        funcBT2.setText("获取");

        funcTV3.setText("删除设备影子");
        funcET3.setText(shadowDelete);
        funcBT3.setText("删除");
        funcRL3.setVisibility(View.VISIBLE);

        funcTV4.setText("监听影子下行");
        funcET4.setEnabled(false);
        funcBT4.setText("监听");
        funcRL4.setVisibility(View.VISIBLE);

        onFunc2Click();
    }

    @Override
    protected void onFunc4Click() {
        listenDownStream();
    }

    /**
     * 删除设备影子
     */
    @Override
    protected void onFunc3Click() {
        String data = funcET3.getText().toString();
        try {
            data = data.replace("{ver}", String.valueOf(++version));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("数据格式不对");
            return;
        }
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, deleteListener);
    }

    private static IConnectSendListener deleteListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            Log.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("设备影子删除成功");
            try {
                if (aRequest instanceof MqttPublishRequest && aResponse != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    Log.d(TAG, "dataStr = " + dataStr);
                    ShadowResponse<String> response = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (response != null && response.version != null && TextUtils.isDigitsOnly(response.version)) {
                        version = Long.valueOf(response.version);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.e(TAG, "update version failed.");
            } catch (Exception e) {
                Log.e(TAG, "update response parse exception.");
            }
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            Log.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("设备影子删除失败");
        }
    };

    /**
     * 获取设备影子
     * 一般开机的时候获取一次 同步 version字段
     */
    @Override
    protected void onFunc2Click() {
        String data = funcET2.getText().toString();
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, getListener);
    }

    private static IConnectSendListener getListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            Log.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("设备影子获取成功");

            try {
                if (aRequest instanceof MqttPublishRequest && aResponse != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    Log.d(TAG, "dataStr = " + dataStr);
                    ShadowResponse<String> response = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (response != null && response.version != null && TextUtils.isDigitsOnly(response.version)) {
                        version = Long.valueOf(response.version);
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.e(TAG, "update version failed.");
            } catch (Exception e) {
                Log.e(TAG, "update response parse exception.");
            }

        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            Log.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("设备影子获取失败");
        }
    };

    /**
     * 上报设备影子
     * 属性变化的时候上报设备影子到云端
     */
    @Override
    protected void onFunc1Click() {
        String data = funcET1.getText().toString();
        try {
            data = data.replace("{ver}", String.valueOf(++version));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("数据格式不对");
            return;
        }
        LinkKit.getInstance().getDeviceShadow().shadowUpload(data, reportListener);
    }

    private static IConnectSendListener reportListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            Log.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            showToast("设备影子更新成功");
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            Log.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("设备影子更新失败");
        }
    };

    /**
     * 先订阅设备影子的更新 topic
     * 云端下发设备影子数据之后
     */
    public void listenDownStream() {
        LinkKit.getInstance().getDeviceShadow().setShadowChangeListener(downListener);
    }

    private static IShadowRRPC downListener = new IShadowRRPC() {
        @Override
        public void onSubscribeSuccess(ARequest aRequest) {
            showToast("设备影子下行订阅成功");
        }

        @Override
        public void onSubscribeFailed(ARequest aRequest, AError aError) {
            showToast("设备影子下行订阅失败");
        }

        @Override
        public void onReceived(ARequest aRequest, AResponse aResponse, IConnectRrpcHandle iConnectRrpcHandle) {
            // TODO user logic
            showToast("收到设备影子下行指令");
            try {
                if (aRequest != null) {
                    String dataStr = null;
                    if (aResponse.data instanceof byte[]) {
                        dataStr = new String((byte[]) aResponse.data, "UTF-8");
                    } else if (aResponse.data instanceof String) {
                        dataStr = (String) aResponse.data;
                    } else {
                        dataStr = String.valueOf(aResponse.data);
                    }
                    Log.d(TAG, "dataStr = " + dataStr);

                    ShadowResponse<String> shadowResponse = JSONObject.parseObject(dataStr, new TypeReference<ShadowResponse<String>>() {
                    }.getType());
                    if (shadowResponse != null && shadowResponse.version != null && TextUtils.isDigitsOnly(shadowResponse.version)) {
                        version = Long.valueOf(shadowResponse.version);
                    }

                    AResponse response = new AResponse();
                    // TODO 用户实现控制设备
                    // 用户控制设备之后 上报影子的值到云端
                    // 上报设置之后的值到云端
                    // 根据当前实际值上报
                    response.data = shadowUpdate.replace("{ver}", String.valueOf(++version));
                    // 第一个值 replyTopic 有默认值 用户不需要设置
                    iConnectRrpcHandle.onRrpcResponse(null, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onResponseSuccess(ARequest aRequest) {
        }

        @Override
        public void onResponseFailed(ARequest aRequest, AError aError) {
        }
    };


    /**
     * 上报更新设备影子
     */
    public void reportShadow(View view) {
        ThreadTools.submitTask(new Runnable() {
            @Override
            public void run() {
                String updateParams = "{\n" + "\"method\": \"update\",\n" + "\"state\": {\"reported\": {\"color\": \"red\"}},\"version\":" + (++version) + "}";
                LinkKit.getInstance().getDeviceShadow().shadowUpload(updateParams, mConnectSendListener);
            }
        }, false);
    }

}
