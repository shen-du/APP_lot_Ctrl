package com.aliyun.alink.devicesdk.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttRrpcRegisterRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttRrpcRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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

public abstract class BaseTemplateActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "BaseTemplateActivity";

    protected RelativeLayout funcRL1, funcRL2, funcRL3, funcRL4, funcRL5, funcRL6;
    protected TextView funcTV1, funcTV2, funcTV3, funcTV4, funcTV5, funcTV6;
    protected EditText funcET1, funcET2, funcET3, funcET4, funcET5, funcET6;
    protected Button funcBT1, funcBT2, funcBT3, funcBT4, funcBT5, funcBT6;
    protected Switch water_switch;
    protected TextView envir_temp,envir_humi,soil_temp,soil_humi,light;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_template);
        initViews();
        initViewData();
    }

    protected abstract void initViewData();

    private void initViews() {
        funcRL1 = findViewById(R.id.func1);
        funcTV1 = findViewById(R.id.func1_name);
        funcET1 = findViewById(R.id.func1_param);
        funcBT1 = findViewById(R.id.func1_exec);
        funcBT1.setOnClickListener(this);

        funcRL2 = findViewById(R.id.func2);
        funcTV2 = findViewById(R.id.func2_name);
        funcET2 = findViewById(R.id.func2_param);
        funcBT2 = findViewById(R.id.func2_exec);
        funcBT2.setOnClickListener(this);

        funcRL3 = findViewById(R.id.func3);
        funcTV3 = findViewById(R.id.func3_name);
        funcET3 = findViewById(R.id.func3_param);
        funcBT3 = findViewById(R.id.func3_exec);
        funcBT3.setOnClickListener(this);

        funcRL4 = findViewById(R.id.func4);
        funcTV4 = findViewById(R.id.func4_name);
        funcET4 = findViewById(R.id.func4_param);
        funcBT4 = findViewById(R.id.func4_exec);
        funcBT4.setOnClickListener(this);

        funcRL5 = findViewById(R.id.func5);
        funcTV5 = findViewById(R.id.func5_name);
        funcET5 = findViewById(R.id.func5_param);
        funcBT5 = findViewById(R.id.func5_exec);
        funcBT5.setOnClickListener(this);

        funcRL6 = findViewById(R.id.func6);
        funcTV6 = findViewById(R.id.func6_name);
        funcET6 = findViewById(R.id.func6_param);
        funcBT6 = findViewById(R.id.func6_exec);
        funcBT6.setOnClickListener(this);

        envir_temp = findViewById(R.id.envir_temp);
        envir_humi = findViewById(R.id.envir_humi);
        soil_temp = findViewById(R.id.soil_temp);
        soil_humi = findViewById(R.id.soil_humi);
        light = findViewById(R.id.light);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.func1_exec:
                onFunc1Click();
                break;
            case R.id.func2_exec:
                onFunc2Click();
                break;
            case R.id.func3_exec:
                onFunc3Click();
                break;
            case R.id.func4_exec:
                onFunc4Click();
                break;
            case R.id.func5_exec:
                onFunc5Click();
                break;
            case R.id.func6_exec:
                onFunc6Click();
                break;
        }
    }

    protected abstract void onFunc1Click();

    protected abstract void onFunc2Click();

    protected void onFunc3Click() {
    }

    protected void onFunc4Click() {
    }

    protected void onFunc5Click() {
    }

    protected void onFunc6Click() {
    }


    protected static IConnectSendListener mConnectSendListener = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            Log.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? null : aResponse.data) + "]");
            if (aRequest instanceof MqttPublishRequest) {
                showToast(((MqttPublishRequest) aRequest).topic + "成功");
                if (((MqttPublishRequest) aRequest).topic != null && ((MqttPublishRequest) aRequest).topic.contains("thing/reset")) {
                    DemoApplication.isInitDone = false;
                }
                return;
            }
            if (aRequest instanceof MqttRrpcRegisterRequest) {
                showToast(((MqttRrpcRegisterRequest) aRequest).topic + "成功");
                return;
            }
            if (aRequest instanceof MqttSubscribeRequest) {
                showToast(((MqttSubscribeRequest) aRequest).topic + "成功");
                return;
            }
            showToast("请求成功");
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            Log.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            if (aRequest instanceof MqttPublishRequest) {
                showToast(((MqttPublishRequest) aRequest).topic + "失败");
                if (((MqttPublishRequest) aRequest).topic != null && ((MqttPublishRequest) aRequest).topic.contains("thing/reset")) {
                    DemoApplication.isInitDone = false;
                }
                return;
            }
            if (aRequest instanceof MqttRrpcRegisterRequest) {
                showToast(((MqttRrpcRegisterRequest) aRequest).topic + "失败");
                return;
            }
            if (aRequest instanceof MqttSubscribeRequest) {
                showToast(((MqttSubscribeRequest) aRequest).topic + "失败");
                return;
            }
            showToast("请求失败");
        }
    };

    protected static IConnectSubscribeListener mSubscribeListener = new IConnectSubscribeListener() {
        @Override
        public void onSuccess() {
            showToast("订阅成功");
        }

        @Override
        public void onFailure(AError aError) {
            showToast("订阅失败");
        }
    };

    protected static IConnectRrpcListener mConnectRrpcListener = new IConnectRrpcListener() {
        @Override
        public void onSubscribeSuccess(ARequest aRequest) {
            Log.d(TAG, "onSubscribeSuccess() called with: aRequest = [" + aRequest + "]");
            showToast("订阅成功");
        }

        @Override
        public void onSubscribeFailed(ARequest aRequest, AError aError) {
            Log.d(TAG, "onSubscribeFailed() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("订阅失败");
        }

        @Override
        public void onReceived(ARequest aRequest, IConnectRrpcHandle iConnectRrpcHandle) {
            Log.d(TAG, "onReceived() called with: aRequest = [" + aRequest + "], iConnectRrpcHandle = [" + iConnectRrpcHandle + "]");
            showToast("接收到下行数据");
            if (aRequest instanceof MqttRrpcRequest){
                // 云端下行数据 拿到
                try {
                    String data = new String((byte[]) (((MqttRrpcRequest) aRequest).payloadObj), "UTF-8");
                    ALog.d(TAG, "payloadObj=" + data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // 远程配置  下推数据示例
            /*{
                "id": "123",
                "version": "1.0",
                "code": 200,
                "data": {
                "configId": "123dagdah",
                    "configSize": 1234565,
                    "sign": "123214adfadgadg",
                    "signMethod": "Sha256",
                    "url": "https://iotx-config.oss-cn-shanghai.aliyuncs.com/xxx",
                    "getType": "file"
                }
            }*/

            // 网关PermitJoin 返回数据示例
            //{"id":"",    //消息id
//                            "version":"1.0",    //ALink协议的版本号
//                            "params":{
            //                        "productKey":"xxx",   //子设备的型号，如果内容为空，表示网关允许任何子设备接入
            //                                "time":60    //网关发现与添加子设备的窗口时间，int类型，单位为秒
//                              }
//                    }


            // 响应获取成功
            // 如果数据是json，且包含id字段，格式可以按照如下示例回复，传输数据请根据实际情况定制
//                if (iConnectRrpcHandle != null){
//                    String topic = null;
//                    if (aRequest instanceof MqttRrpcRequest) {
//                        topic = ((MqttRrpcRequest) aRequest).topic;
//                    }
//                    if (TextUtils.isEmpty(topic)) {
//                        return;
//                    }
//                    AResponse aResponse = new AResponse();
//                    // 仅供参考，具体返回云端的数据用户根据实际场景添加到data结构体
////                    aResponse.data = "{\"id\":\"" + 123 + "\", \"code\":\"200\"" + ",\"data\":{} }";
//                    iConnectRrpcHandle.onRrpcResponse(topic, aResponse);
//                }

            // 如果不一定是json格式，可以参考如下方式回复
            MqttPublishRequest rrpcResponse = new MqttPublishRequest();
            if (aRequest instanceof MqttRrpcRequest) {
                rrpcResponse.topic = ((MqttRrpcRequest) aRequest).topic;
            }
            rrpcResponse.payloadObj ="{\"id\":\"123\", \"code\":\"200\"" + ",\"data\":{} }";

            LinkKit.getInstance().publish(rrpcResponse, new IConnectSendListener() {
                @Override
                public void onResponse(ARequest aRequest, AResponse aResponse) {
                    ALog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + aResponse + "]");
                }

                @Override
                public void onFailure(ARequest aRequest, AError aError) {
                    ALog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
                }
            });

        }

        @Override
        public void onResponseSuccess(ARequest aRequest) {
            Log.d(TAG, "onResponseSuccess() called with: aRequest = [" + aRequest + "]");
        }

        @Override
        public void onResponseFailed(ARequest aRequest, AError aError) {
            Log.d(TAG, "onResponseFailed() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
        }
    };
}
