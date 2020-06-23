package com.aliyun.alink.devicesdk.demo;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.devicesdk.manager.InitManager;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttSubscribeRequest;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSubscribeListener;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.log.IDGenerater;

import java.util.Timer;
import java.util.TimerTask;

public class MqttActivity extends BaseTemplateActivity {
    private static final String TAG = "MqttActivity";
    private static int i=0;
    Timer timer = new Timer();
    TimerTask task = new TimerTask() {//定时刷新数据界面
        public void run() {
            display();
        }
    };

    @Override
    protected void initViewData() {
        funcTV1.setText("");
        // 请先确认云端对应产品有该topic，再打开进行调试
        String testSubscribeTopicAndQos = "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/information:0";
        funcTV1.setText("格式：topic:qos");
        funcET1.setText(testSubscribeTopicAndQos);
        funcBT1.setText("订阅");
        funcBT1.setVisibility(View.INVISIBLE);
        funcTV1.setVisibility(View.INVISIBLE);
        funcET1.setVisibility(View.INVISIBLE);
        // 请先确认云端对应产品有该topic，再打开进行调试
        String testPublishTopic = "/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/control:0";
        funcET2.setText(testPublishTopic);
        funcTV2.setHint("格式：topic:qos");
        funcET2.setText(testPublishTopic);
//        funcET2.setText("0");
        funcBT2.setText("发布");
        funcBT2.setVisibility(View.INVISIBLE);
        funcTV2.setVisibility(View.INVISIBLE);
        funcET2.setVisibility(View.INVISIBLE);

        /**
         * 请先确认云端对应产品有该topic，再打开进行调试
         *
         * RRPC 是云端向设备请求服务的过程，对于设备端来说包含以下两个过程：
         * 1. 订阅需要响应的服务； 一般是通配订阅，"/ext/rrpc/+/${pk}/${dn}/${用户自定义部分}"
         * 2. 接收并响应；在全局下行onNotify监听符合这个格式的请求topic，并响应，参考IniitManager-> onNotify 对自定义RRPC的处理
         */
        String rrpcTopic = "/ext/rrpc/+/" + DemoApplication.productKey + "/" + DemoApplication.deviceName + "/user/xxx";
        funcTV3.setHint("RRPC");
        funcBT3.setText("RRPC示例");
        funcET3.setText(rrpcTopic);
        funcRL3.setVisibility(View.VISIBLE);
        sub();


        timer.schedule(task, 1000 * 1, 1000 * 2); //启动timer

        water_switch = findViewById(R.id.water_switch);
        water_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    send_control(isChecked);water_switch.setText("水泵开");
                    //Toast.makeText(getApplicationContext(), "开", Toast.LENGTH_SHORT).show();
                }
                else{
                    send_control(isChecked);water_switch.setText("水泵关");
                    //Toast.makeText(getApplicationContext(), "关", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        onFunc1Click();
//        onFunc2Click();
    }
    public void display() {
        envir_temp.setText(Float.toString(InitManager.EnvirTemp)+"℃");
        envir_humi.setText(Float.toString(InitManager.EnvirHumi)+"%");
        soil_temp.setText(Float.toString(InitManager.SoilTemp)+"℃");
        soil_humi.setText(Float.toString(InitManager.SoilHumi)+"%");
        light.setText(Float.toString(InitManager.light)+"lux");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onFunc1Click() {
        try {
            String topicAndQos = funcET1.getText().toString();
            if (TextUtils.isEmpty(topicAndQos)) {
                showToast("请根据自身产品设置需要测试订阅的topic");
                return;
            }
            if (!topicAndQos.contains(":")) {
                showToast("格式不合法，必须包含qos（qos只能是0或1）");
                return;
            }
            String[] topicQosArray = topicAndQos.split(":");
            String topic = topicQosArray[0];
            String qosString = topicQosArray[1];
            if (TextUtils.isEmpty(topic)) {
                showToast("格式不合法，topic不能为空");
                return;
            }
            if (TextUtils.isEmpty(qosString)) {
                showToast("格式不合法，qos不能为空");
                return;
            }

            int qos = 0;
            try {
                qos = Integer.parseInt(qosString);
                if (qos != 0 && qos != 1) {
                    showToast("qos值非法，设置为0或1");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("qos值非法，请设置为0或1");
                return;
            }

            MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
            subscribeRequest.isSubscribe = true;
            subscribeRequest.topic = topic;
            subscribeRequest.qos = qos;
            LinkKit.getInstance().subscribe(subscribeRequest, BaseTemplateActivity.mSubscribeListener);
        } catch (Exception e) {
            showToast("数据异常");
        }
    }
    public void sub(){
        try {
            String topicAndQos = funcET1.getText().toString();
            if (TextUtils.isEmpty(topicAndQos)) {
                showToast("请根据自身产品设置需要测试订阅的topic");
                return;
            }
            if (!topicAndQos.contains(":")) {
                showToast("格式不合法，必须包含qos（qos只能是0或1）");
                return;
            }
            String[] topicQosArray = topicAndQos.split(":");
            String topic = topicQosArray[0];
            String qosString = topicQosArray[1];
            if (TextUtils.isEmpty(topic)) {
                showToast("格式不合法，topic不能为空");
                return;
            }
            if (TextUtils.isEmpty(qosString)) {
                showToast("格式不合法，qos不能为空");
                return;
            }

            int qos = 0;
            try {
                qos = Integer.parseInt(qosString);
                if (qos != 0 && qos != 1) {
                    showToast("qos值非法，设置为0或1");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("qos值非法，请设置为0或1");
                return;
            }

            MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
            subscribeRequest.isSubscribe = true;
            subscribeRequest.topic = topic;
            subscribeRequest.qos = qos;
            LinkKit.getInstance().subscribe(subscribeRequest, BaseTemplateActivity.mSubscribeListener);
        } catch (Exception e) {
            showToast("数据异常");
        }
    }
    @Override
    protected void onFunc2Click() {
        try {
            String topicAndQos = funcET2.getText().toString();
            if (TextUtils.isEmpty(topicAndQos)) {
                showToast("请根据自身产品设置需要测试订阅的topic");
                return;
            }
            if (!topicAndQos.contains(":")) {
                showToast("格式不合法，必须包含qos（qos只能是0或1）");
                return;
            }
            String[] topicQosArray = topicAndQos.split(":");
            String topic = topicQosArray[0];
            String qosString = topicQosArray[1];
            if (TextUtils.isEmpty(topic)) {
                showToast("格式不合法，topic不能为空");
                return;
            }
            if (TextUtils.isEmpty(qosString)) {
                showToast("格式不合法，qos不能为空");
                return;
            }

            int qos = 0;
            try {
                qos = Integer.parseInt(qosString);
                if (qos != 0 && qos != 1) {
                    showToast("qos值非法，设置为0或1");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("qos值非法，请设置为0或1");
                return;
            }

            MqttPublishRequest request = new MqttPublishRequest();
            request.qos = qos;
            request.isRPC = false;
            request.topic = topic;
            request.msgId = String.valueOf(IDGenerater.generateId());
            // TODO 用户根据实际情况填写 仅做参考
            request.payloadObj ="{\"switch\":\"1\" }";
            LinkKit.getInstance().publish(request, BaseTemplateActivity.mConnectSendListener);
        } catch (Exception e) {
            showToast("发布异常 ");
        }
    }
    public void send_control(boolean control)
    {
        try {
            String topicAndQos = funcET2.getText().toString();
            if (TextUtils.isEmpty(topicAndQos)) {
                showToast("请根据自身产品设置需要测试订阅的topic");
                return;
            }
            if (!topicAndQos.contains(":")) {
                showToast("格式不合法，必须包含qos（qos只能是0或1）");
                return;
            }
            String[] topicQosArray = topicAndQos.split(":");
            String topic = topicQosArray[0];
            String qosString = topicQosArray[1];
            if (TextUtils.isEmpty(topic)) {
                showToast("格式不合法，topic不能为空");
                return;
            }
            if (TextUtils.isEmpty(qosString)) {
                showToast("格式不合法，qos不能为空");
                return;
            }

            int qos = 0;
            try {
                qos = Integer.parseInt(qosString);
                if (qos != 0 && qos != 1) {
                    showToast("qos值非法，设置为0或1");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("qos值非法，请设置为0或1");
                return;
            }

            MqttPublishRequest request = new MqttPublishRequest();
            request.qos = qos;
            request.isRPC = false;
            request.topic = topic;
            request.msgId = String.valueOf(IDGenerater.generateId());
            // TODO 用户根据实际情况填写 仅做参考
            request.payloadObj ="{\"switch\":\""+control+"\" }";
            LinkKit.getInstance().publish(request, BaseTemplateActivity.mConnectSendListener);
        } catch (Exception e) {
            showToast("发布异常 ");
        }
    }
    @Override
    protected void onFunc3Click() {
        if (TextUtils.isEmpty(funcET3.getText().toString())) {
            showToast("请根据自身产品设置需要测试RRPC的topic");
            return;
        }
        String testTopic = funcET3.getText().toString();
        if (TextUtils.isEmpty(testTopic)) {
            showToast("topic 不可为空");
            return;
        }
        // 通配topic 如 /ext/rrpc/+/pk/dn/user/get
        // "/sys/" + productKey + "/" + deviceName + "/rrpc/request/+"

        // rrpcReplyTopic 替换成用户自己定义的RRPC 响应 topic
        MqttSubscribeRequest subscribeRequest = new MqttSubscribeRequest();
        subscribeRequest.isSubscribe = true;
        subscribeRequest.topic = testTopic;
        LinkKit.getInstance().subscribe(subscribeRequest, new IConnectSubscribeListener() {
                    @Override
                    public void onSuccess() {
                        // 订阅成功
                        showToast("订阅成功");
                    }

                    @Override
                    public void onFailure(AError aError) {
                        // 订阅失败
                        showToast("订阅失败");
                    }
                }
        );
    }

    @Override
    protected void onFunc4Click() {


    }
}
