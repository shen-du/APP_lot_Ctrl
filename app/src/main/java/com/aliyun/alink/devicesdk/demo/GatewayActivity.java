package com.aliyun.alink.devicesdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.devicesdk.adapter.SubDeviceListAdapter;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.SignUtils;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.dm.model.ResponseModel;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.channel.gateway.api.GatewayChannel;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceActionListener;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceChannel;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceConnectListener;
import com.aliyun.alink.linksdk.channel.gateway.api.subdevice.ISubDeviceRemoveListener;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttPublishRequest;
import com.aliyun.alink.linksdk.cmp.connect.channel.MqttRrpcRequest;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcHandle;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectRrpcListener;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.cmp.core.util.RandomStringUtil;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.aliyun.alink.linksdk.tools.log.IDGenerater;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *
 *  Copyright (c) 2014-2016 Alibaba Group. All rights reserved.
 *  License-Identifier: Apache-2.0
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


public class GatewayActivity extends BaseActivity {
    private static final String TAG = "GatewayActivity";

    private List<DeviceInfo> subDeviceTripleInfoList = new ArrayList<>();

    private String testPublishTopic = "/sys/{productKey}/{deviceName}/thing/event/property/post";
    private String testSubscribePropertyService = "/sys/{productKey}/{deviceName}/thing/service/property/set";
    private String testSubscribeService = "/sys/{productKey}/{deviceName}/thing/service/+";
    private String testSubscribeSyncService = "/sys/{productKey}/{deviceName}/rrpc/request/+";
    private String[] subscribeServiceList = {testSubscribePropertyService, testSubscribeService, testSubscribeSyncService};


    private Spinner subDeviceListSpinner = null;
    private DeviceInfo selectedSubdeviceInfo = null;
    private SubDeviceListAdapter subDeviceListAdapter = null;
    private EditText publishPayloadET = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway);
        initViews();
        addPermitJoinSupport();
    }

    private void addPermitJoinSupport() {
        LinkKit.getInstance().getGateway().permitJoin(BaseTemplateActivity.mConnectRrpcListener);
    }

    private void initViews() {
//        publishPayloadET = findViewById(R.id.id_publish_payload);
        subDeviceListSpinner = findViewById(R.id.id_sub_dev_list);
        subDeviceListAdapter = new SubDeviceListAdapter(this);
        subDeviceListSpinner.setAdapter(subDeviceListAdapter);
        subDeviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected() called with: parent = [" + parent + "], view = [" + view + "], position = [" + position + "], id = [" + id + "]");
                if (subDeviceListAdapter == null) {
                    return;
                }
                if (subDeviceListAdapter.getCount() < 1 || position < 0 || position > subDeviceListAdapter.getCount() - 1) {
                    Log.w(TAG, "position invalid, position=" + position + ",count=" + subDeviceListAdapter.getCount());
                    return;
                }
                selectedSubdeviceInfo = (DeviceInfo) subDeviceListAdapter.getItem(position);
                if (selectedSubdeviceInfo == null) {
                    Log.w(TAG, "selected device info is null.");
                    return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected() called with: parent = [" + parent + "]");

            }
        });
    }


    private void updateSpinnerList(final List<DeviceInfo> infoList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (subDeviceListAdapter != null) {
                    subDeviceListAdapter.setListData(infoList);
                    subDeviceListAdapter.notifyDataSetChanged();
                    if (selectedSubdeviceInfo == null && infoList.size() > 0) {
                        selectedSubdeviceInfo = (DeviceInfo) subDeviceListAdapter.getItem(0);
                    }
                }
            }
        });
    }

    /**
     * 子设备动态注册
     *
     * @param view 云端安全策略问题  需要先在云端创建 子设备
     */
    public void subDevRegister(View view) {
        List<DeviceInfo> toRegisterDeviceList = getSubDevList();
        if (toRegisterDeviceList == null || toRegisterDeviceList.size() < 1) {
            showToast("子设备待注册列表为空");
            return;
        }
        List<DeviceInfo> presetSubdevList = new ArrayList<>();
        List<DeviceInfo> normalSubDevList = new ArrayList<>();
        DeviceInfo item = null;
        for (int i = 0; i < toRegisterDeviceList.size(); i++) {
            item = toRegisterDeviceList.get(i);
            if (item == null) {
                continue;
            }
            if (!TextUtils.isEmpty(item.productSecret)) {
                presetSubdevList.add(toRegisterDeviceList.get(i));
            } else {
                normalSubDevList.add(toRegisterDeviceList.get(i));
            }
        }

        if (presetSubdevList.size() > 0) {
            // 该动态注册方案需要提前知道子设备的productSecret，安全性会比下面一种子设备动态注册低一点
            // 这种动态注册方式可以考虑和COTA-远程配置下发配合使用，在云端下发子设备的pk、dn、ps，网关收到后
            // 完成动态注册
            // 使用于需要抢占绑定关系时使用，会返回被其他网关设备的子设备
            MqttPublishRequest request = new MqttPublishRequest();
            final RequestModel requestModel = new RequestModel();
            requestModel.id = String.valueOf(IDGenerater.generateId());
            requestModel.version = "1.0";
            requestModel.method = GatewayChannel.METHOD_PRESET_SUBDEV_REGITER;
            request.isRPC = true;
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < presetSubdevList.size(); i++) {
                DeviceInfo itemDev = presetSubdevList.get(i);
                Map<String, String> itemMap = new HashMap<>();
                itemMap.put("productKey", itemDev.productKey);
                itemMap.put("deviceName", itemDev.deviceName);
                itemMap.put("random", RandomStringUtil.getRandomString(10));
                String sign = SignUtils.hmacSign(itemMap, itemDev.productSecret);
                itemMap.put("sign", sign);
                itemMap.put("signMethod", "hmacsha1");
                jsonArray.add(itemMap);
            }

            jsonObject.put("proxieds", jsonArray);
            requestModel.params = jsonObject;
            request.payloadObj = requestModel.toString();
            LinkKit.getInstance().getGateway().subDevicRegister(request, enhanceSendListener);
        }

        if (normalSubDevList.size() > 0) {
            // 适用于只需要之设备的pk、dn进行动态添加上线的方案
            // 如果被其他 网关设备绑定了，动态注册结果里面不会包含子设备的三元组信息
            LinkKit.getInstance().getGateway().gatewaySubDevicRegister(normalSubDevList, normalSendListener);
        }
    }

    private IConnectSendListener enhanceRegister = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            ALog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + aResponse + "]");
            try {
                showToast("收到子设备动态结果");
                ResponseModel<Map<String, List<DeviceInfo>>> responseModel = JSONObject.parseObject(aResponse.data.toString(),
                        new TypeReference<ResponseModel<Map<String, List<DeviceInfo>>>>() {
                        }.getType());
                // TODO 保存子设备的三元组信息
                ALog.d(TAG, "onResponse responseModel=" + JSONObject.toJSONString(responseModel));
                // {"code":200,"data":{"failures":[],"successes":[{"deviceSecret":"xxx","productKey":"xxx","deviceName":"xxx"}]},"id":"1","message":"success","method":"thing.proxy.provisioning.product_register","version":"1.0"}
                List<DeviceInfo> successList = responseModel.data.get("successes");
                List<DeviceInfo> failList = responseModel.data.get("failures");
                selectedSubdeviceInfo = null;
                updateSpinnerList(successList);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            ALog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
        }
    };

    private IConnectSendListener normalRegister = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            ALog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? "null" : aResponse.data) + "]");
            try {
                showToast("子设备动态注册成功");
                ResponseModel<List<DeviceInfo>> response = JSONObject.parseObject(aResponse.data.toString(), new TypeReference<ResponseModel<List<DeviceInfo>>>() {
                }.getType());
                //TODO 保存子设备的三元组信息
                // for test
                selectedSubdeviceInfo = null;
                updateSpinnerList(response.data);
                log(TAG, "子设备动态注册成功 " + response.data.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            ALog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("子设备动态注册失败");
            log(TAG, "子设备动态注册失败");
        }
    };

    private SendListener normalSendListener = new SendListener(normalRegister);
    private SendListener enhanceSendListener = new SendListener(enhanceRegister);

    static class SendListener implements IConnectSendListener{
        WeakReference<IConnectSendListener> handlerWakRef = null;

        public SendListener(IConnectSendListener handler) {
            handlerWakRef = new WeakReference<>(handler);
        }
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            if (handlerWakRef != null && handlerWakRef.get() != null) {
                handlerWakRef.get().onResponse(aRequest, aResponse);
            }
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            if (handlerWakRef != null && handlerWakRef.get() != null) {
                handlerWakRef.get().onFailure(aRequest, aError);
            }
        }
    }

    /**
     * 获取当前网关的子设备列表
     * 需要先添加子设备到网关
     *
     * @param view
     */
    public void getSubDevices(View view) {
        LinkKit.getInstance().getGateway().gatewayGetSubDevices(getSubDevSendListener);
    }

    private IConnectSendListener getSubDev = new IConnectSendListener() {
        @Override
        public void onResponse(ARequest aRequest, AResponse aResponse) {
            ALog.d(TAG, "onResponse() called with: aRequest = [" + aRequest + "], aResponse = [" + (aResponse == null ? "null" : aResponse.data) + "]");
            showToast("获取子设备列表成功");
            log(TAG, "获取子设备列表成功 aResponse=" + (aResponse == null ? "null" : aResponse.data));
            try {
                ResponseModel<List<DeviceInfo>> response = JSONObject.parseObject(aResponse.data.toString(), new TypeReference<ResponseModel<List<DeviceInfo>>>() {
                }.getType());

                subDeviceTripleInfoList.addAll(response.data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(ARequest aRequest, AError aError) {
            ALog.d(TAG, "onFailure() called with: aRequest = [" + aRequest + "], aError = [" + aError + "]");
            showToast("获取子设备列表失败");
            log(TAG, "获取子设备列表失败");
        }
    };

    private SendListener getSubDevSendListener = new SendListener(getSubDev);

    /**
     * 添加子设备到网关
     * 子设备动态注册之后　可以拿到子设备的 deviceSecret 信息，签名的时候需要使用到
     * 签名方式 sign = hmac_md5(deviceSecret, clientId123deviceNametestproductKey123timestamp1524448722000)
     *
     * @param view
     */
    public void addSubDevice(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，添加失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        LinkKit.getInstance().getGateway().gatewayAddSubDevice(info, subDevConnectListener);
    }

    static class SubDevConnectListener implements ISubDeviceConnectListener{
        ISubDeviceConnectListener handlerConnectListener = null;

        public SubDevConnectListener(ISubDeviceConnectListener handler) {
            handlerConnectListener = handler;
        }

        @Override
        public String getSignMethod() {
            if (handlerConnectListener != null) {
                return handlerConnectListener.getSignMethod();
            }
            return null;
        }

        @Override
        public String getSignValue() {
            if (handlerConnectListener != null) {
                return handlerConnectListener.getSignValue();
            }
            return null;
        }

        @Override
        public String getClientId() {
            if (handlerConnectListener != null) {
                return handlerConnectListener.getClientId();
            }
            return null;
        }

        @Override
        public Map<String, Object> getSignExtraData() {
            if (handlerConnectListener != null) {
                return handlerConnectListener.getSignExtraData();
            }
            return null;
        }

        @Override
        public void onConnectResult(boolean b, ISubDeviceChannel iSubDeviceChannel, AError aError) {
            if (handlerConnectListener != null) {
                handlerConnectListener.onConnectResult(b, iSubDeviceChannel, aError);
            }
        }

        @Override
        public void onDataPush(String s, AMessage aMessage) {
            if (handlerConnectListener != null) {
                handlerConnectListener.onDataPush(s, aMessage);
            }
        }
    }

    /**
     * 删除子设备
     *
     * @param view
     */
    private void deleteSubDevice(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，删除失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        LinkKit.getInstance().getGateway().gatewayDeleteSubDevice(info, removeListener);
    }

    private static ISubDeviceRemoveListener removeListener = new ISubDeviceRemoveListener() {
        @Override
        public void onSuceess() {
            ALog.d(TAG, "onSuceess() called");
            showToast("成功删除子设备 ");
        }

        @Override
        public void onFailed(AError aError) {
            ALog.d(TAG, "onFailed() called with: aError = [" + aError + "]");
            showToast("删除子设备失败");
        }
    };

    private ISubDeviceConnectListener connectListener = new ISubDeviceConnectListener() {
        @Override
        public String getSignMethod() {
            ALog.d(TAG, "getSignMethod() called");
            return "hmacsha1";
        }

        @Override
        public String getSignValue() {
            ALog.d(TAG, "getSignValue() called");
            if (selectedSubdeviceInfo == null) {
                return null;
            }
            Map<String, String> signMap = new HashMap<>();
            signMap.put("productKey", selectedSubdeviceInfo.productKey);
            signMap.put("deviceName", selectedSubdeviceInfo.deviceName);
//                signMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
            signMap.put("clientId", getClientId());
            return SignUtils.hmacSign(signMap, selectedSubdeviceInfo.deviceSecret);
        }

        @Override
        public String getClientId() {
            ALog.d(TAG, "getClientId() called");
            return "id";
        }

        @Override
        public Map<String, Object> getSignExtraData() {
            return null;
        }

        @Override
        public void onConnectResult(boolean isSuccess, ISubDeviceChannel iSubDeviceChannel, AError aError) {
            ALog.d(TAG, "onConnectResult() called with: isSuccess = [" + isSuccess + "], iSubDeviceChannel = [" + iSubDeviceChannel + "], aError = [" + aError + "]");
            if (isSuccess) {
                showToast("子设备添加成功");
                log(TAG, "子设备添加成功 " + getPkDn(selectedSubdeviceInfo));
                subDevOnline(null);
            }
        }

        @Override
        public void onDataPush(String s, AMessage message) {
            // new String((byte[]) message.getData())
            // {"method":"thing.service.property.set","id":"184220091","params":{"test":2},"version":"1.0.0"} 示例
            ALog.d(TAG, "收到子设备下行数据  onDataPush() called with: s = [" + s + "], s1 = [" + message + "]");
        }
    };

    private SubDevConnectListener subDevConnectListener = new SubDevConnectListener(connectListener);

    /**
     * 网关添加子设备之后才能代理子设备上线
     *
     * @param view
     */
    private void subDevOnline(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，上线失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        LinkKit.getInstance().getGateway().gatewaySubDeviceLogin(info, subDeviceActionListener);
    }

    /**
     * 网关添加子设备之后才能代理子设备下线
     *
     * @param view
     */
    public void subDevOffline(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注F册的设备，下线失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        LinkKit.getInstance().getGateway().gatewaySubDeviceLogout(info, new ISubDeviceActionListener() {
            @Override
            public void onSuccess() {
                showToast("子设备下线成功");
                log(TAG, "子设备下线成功");
                deleteSubDevice(null);
            }

            @Override
            public void onFailed(AError aError) {
                showToast("子设备下线失败");
                log(TAG, "子设备下线失败");
                deleteSubDevice(null);
            }
        });
    }

    /**
     * 代理子设备订阅
     *
     * @param view
     */
    public void subDevSubscribe(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，代理子设备订阅失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        String topic = null;
//        for (int i = 0; i < subscribeServiceList.length; i++) {
            topic = subscribeServiceList[0];

            final String tempTopic = topic.replace("{deviceName}", selectedSubdeviceInfo.deviceName)
                    .replace("{productKey}", selectedSubdeviceInfo.productKey);

            LinkKit.getInstance().getGateway().gatewaySubDeviceSubscribe(tempTopic, info, subDeviceActionListener);
//        }
    }

    /**
     * 代理子设备发布
     *
     * @param view
     */
    public void subDevPublish(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，代理子设备发布失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        String topic = testPublishTopic.replace("{deviceName}", selectedSubdeviceInfo.deviceName)
                .replace("{productKey}", selectedSubdeviceInfo.productKey);
        String data = publishPayloadET.getText().toString();
        LinkKit.getInstance().getGateway().gatewaySubDevicePublish(topic, data, info, subDeviceActionListener);
    }

    /**
     * 代理子设备取消订阅
     *
     * @param view
     */
    public void subDevUnsubscribe(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，代理子设备取消订阅失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        String topic = testSubscribePropertyService.replace("{deviceName}", selectedSubdeviceInfo.deviceName)
                .replace("{productKey}", selectedSubdeviceInfo.productKey);
        LinkKit.getInstance().getGateway().gatewaySubDeviceUnsubscribe(topic, info, subDeviceActionListener);
    }

    private static ISubDeviceActionListener subDeviceActionListener = new ISubDeviceActionListener() {
        @Override
        public void onSuccess() {
            ALog.d(TAG, "onSuccess() called");
            showToast("代理子设备执行成功");
        }

        @Override
        public void onFailed(AError aError) {
            ALog.d(TAG, "onFailed() called with: aError = [" + aError + "]");
            showToast("代理子设备执行失败");
        }
    };

    private List<DeviceInfo> getSubDevList() {
        return DemoApplication.mDeviceInfoData.subDevice;
    }

    private void subDevDisable(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，注册禁用监听失败");
            return;
        }
        final DeviceInfo info = selectedSubdeviceInfo;
        LinkKit.getInstance().getGateway().gatewaySetSubDeviceDisableListener(info, BaseTemplateActivity.mConnectRrpcListener);
    }

    private String getPkDn(DeviceInfo info) {
        if (info == null) {
            return null;
        }
        return "[pk=" + info.productKey + ",dn=" + info.deviceName + "]";
    }

    public void startSubDeviceControl(View view) {
        if (selectedSubdeviceInfo == null || TextUtils.isEmpty(selectedSubdeviceInfo.productKey) ||
                TextUtils.isEmpty(selectedSubdeviceInfo.deviceName)) {
            showToast("无有效已动态注册的设备，订阅设备删除通知失败");
            return;
        }
        showToast("请在子设备需要添加拓扑并登录成功后进入");

        final DeviceInfo info = selectedSubdeviceInfo;
        Intent intent = new Intent(this, ControlPannelActivity.class);
        intent.putExtra("pk", info.productKey);
        intent.putExtra("dn", info.deviceName);
        intent.putExtra("sub", true);
        startActivity(intent);
    }
}
