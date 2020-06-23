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
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.alink.devicesdk.adapter.EventListAdapter;
import com.aliyun.alink.devicesdk.adapter.PropertyListAdapter;
import com.aliyun.alink.devicesdk.app.DemoApplication;
import com.aliyun.alink.dm.api.BaseInfo;
import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.IDMCallback;
import com.aliyun.alink.dm.api.IThing;
import com.aliyun.alink.dm.api.InitResult;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.tmp.api.InputParams;
import com.aliyun.alink.linksdk.tmp.api.OutputParams;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tmp.devicemodel.Arg;
import com.aliyun.alink.linksdk.tmp.devicemodel.Event;
import com.aliyun.alink.linksdk.tmp.devicemodel.Property;
import com.aliyun.alink.linksdk.tmp.devicemodel.Service;
import com.aliyun.alink.linksdk.tmp.listener.IDevRawDataListener;
import com.aliyun.alink.linksdk.tmp.listener.IPublishResourceListener;
import com.aliyun.alink.linksdk.tmp.listener.ITResRequestHandler;
import com.aliyun.alink.linksdk.tmp.listener.ITResResponseCallback;
import com.aliyun.alink.linksdk.tmp.utils.ErrorInfo;
import com.aliyun.alink.linksdk.tmp.utils.GsonUtils;
import com.aliyun.alink.linksdk.tmp.utils.TmpConstant;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

public class ControlPannelActivity extends BaseActivity {
    private static final String TAG = "ControlPannelActivity";

    private Spinner mPropertySpinner = null;
    private Spinner mEventSpinner = null;
    private EditText mPropertyET = null;
    private EditText mEventET = null;
    private TextView mDeiviceTypeTV = null;
    private PropertyListAdapter adapter = null;
    private EventListAdapter eventAdapter = null;
    private List<Property> propertyList = null;
    private Map<String, ValueWrapper> reportData = null;
    private boolean isSubDev = false;

    private final static String SERVICE_SET = "set";
    private final static String SERVICE_GET = "get";
    private final static String CONNECT_ID = "LINK_PERSISTENT";

    final static Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");

    private final static int DEF_VALUE = Integer.MIN_VALUE;

    private BaseInfo mBaseInfo = new BaseInfo();
    private String productKey = null;
    private String deviceName = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_pannel);
        reportData = new HashMap<>();
        parseIntent();
        if (!isSubDev) {
            setServiceHandler();
        }
        initViews();
        // 测试二进制数据上报
        if (!isSubDev) {
            thingRawPropertiesPost();
        } else {
            initSubDeviceThingModel();
        }
    }

    private void parseIntent() {
        Intent intent = getIntent();

        try {
            if (intent == null || intent.getExtras() == null){
                ALog.d(TAG, "intent with no data. Non sub device.");
                productKey = DemoApplication.productKey;
                deviceName = DemoApplication.deviceName;
                isSubDev = false;
                return;
            }
            productKey = intent.getExtras().getString("pk", DemoApplication.productKey);
            deviceName = intent.getExtras().getString("dn", DemoApplication.deviceName);
            isSubDev = intent.getExtras().getBoolean("sub", false);
            if (isSubDev) {
                mBaseInfo.productKey = productKey;
                mBaseInfo.deviceName = deviceName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSubDeviceThingModel() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.productKey = productKey;
        deviceInfo.deviceName = deviceName;
//        deviceInfo.deviceSecret = "xxxx"; //TODO
        Map<String, ValueWrapper> subDevInitState = new HashMap<>();
//        subDevInitState.put(); //TODO
        LinkKit.getInstance().getGateway().initSubDeviceThing(null, deviceInfo, subDevInitState, new IDMCallback<InitResult>() {
            @Override
            public void onSuccess(InitResult initResult) {
                Log.d(TAG, "onSuccess() called with: initResult = [" + initResult + "]");
                showToast("子设备初始化完成");
                setServiceHandler();
                initValues();
            }

            @Override
            public void onFailure(AError aError) {
                Log.d(TAG, "onFailure() called with: aError = [" + (aError==null?null:(aError.getCode()+aError.getMsg())) + "]");
                showToast("子设备初始化失败");
            }
        });
    }

    private void initViews() {
        mPropertySpinner = findViewById(R.id.property_spinner);
        mEventSpinner = findViewById(R.id.event_spinner);

        mPropertyET = findViewById(R.id.value_text);
        mEventET = findViewById(R.id.event_value);

        mDeiviceTypeTV = findViewById(R.id.device_name);
        adapter = new PropertyListAdapter(this);
        mPropertySpinner.setAdapter(adapter);
        if (!isSubDev) {
            adapter.setListData(LinkKit.getInstance().getDeviceThing().getProperties());
            adapter.notifyDataSetChanged();
        }
        mPropertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "property onItemSelected() called with: parent = [" + parent + "], view = [" + view + "], position = [" + position + "], id = [" + id + "]");
                Property property = (Property) adapter.getItem(position);
                updatePropertyValue(property);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mPropertyET.setText("");
            }
        });
        eventAdapter = new EventListAdapter(this);
        mEventSpinner.setAdapter(eventAdapter);
        if (!isSubDev) {
            eventAdapter.setListData(LinkKit.getInstance().getDeviceThing().getEvents());
            eventAdapter.notifyDataSetChanged();
        }
        mEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "event onItemSelected() called with: parent = [" + parent + "], view = [" + view + "], position = [" + position + "], id = [" + id + "]");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mEventET.setText("");
            }
        });
    }

    private boolean isValidDouble(String value) {
        if (TextUtils.isEmpty(value)) {
            return false;
        }
        try {
            if (pattern != null && pattern.matcher(value) != null) {
                if (pattern.matcher(value).matches()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Double getDouble(String value) {
        if (isValidDouble(value)) {
            return Double.parseDouble(value);
        }
        return null;
    }

    private boolean isValidInt(String value) {
        return !TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value);
    }


    private int getInt(String value) {
        if (isValidInt(value)) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return DEF_VALUE;
    }

    private void initValues() {
        if (isSubDev) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    IThing thing = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first;
                    if (thing != null) {
                        adapter.setListData(thing.getProperties());
                        adapter.notifyDataSetChanged();
                        eventAdapter.setListData(thing.getEvents());
                        eventAdapter.notifyDataSetChanged();
                        mDeiviceTypeTV.setText(String.format(getResources().getString(R.string.control_pannel_device_name), productKey));
                    }
                }
            });
            return;
        }
        propertyList = LinkKit.getInstance().getDeviceThing().getProperties();
        adapter.setListData(propertyList);
        adapter.notifyDataSetChanged();
        mDeiviceTypeTV.setText(String.format(getResources().getString(R.string.control_pannel_device_name), productKey));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initValues();
//        connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void report(String identifier, ValueWrapper valueWrapper) {
        reportData.clear();
        Map<String, ValueWrapper> reportData  = new HashMap<>();
        reportData.put(identifier, valueWrapper);
        if (!isSubDev) {
            try {
                LinkKit.getInstance().getDeviceThing().thingPropertyPost(reportData, resourceListener);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("上报失败 " + e);
            }
        } else {
            try {
                IThing thing = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first;
                if (thing == null){
                    showToast("子设备当前状态不支持");
                    return;
                }
                thing.thingPropertyPost(reportData, resourceListener);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("上报失败 " + e);
            }
        }
    }

    private void report(Map<String, ValueWrapper> dataWrapper) {
        reportData.clear();
        reportData.putAll(dataWrapper);
        if (!isSubDev) {
            try {
                LinkKit.getInstance().getDeviceThing().thingPropertyPost(reportData, resourceListener);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("上报失败 " + e);
            }
        } else {
            try {
                IThing thing = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first;
                if (thing == null){
                    showToast("子设备当前状态不支持");
                    return;
                }
                thing.thingPropertyPost(reportData, resourceListener);
            } catch (Exception e) {
                e.printStackTrace();
                showToast("上报失败 " + e);
            }
        }
    }

    private static IPublishResourceListener resourceListener = new IPublishResourceListener() {
        @Override
        public void onSuccess(String s, Object o) {
            Log.d(TAG, "onSuccess() called with: s = [" + s + "], o = [" + o + "]");
            showToast("设备状态上报上行成功（code=200），用户可以根据云端返回的data判断是否有不符合的属性上报");
        }

        @Override
        public void onError(String s, AError aError) {
            Log.d(TAG, "onError() called with: s = [" + s + "], aError = [" + aError + "]");
            showToast("设备上报状态失败");
        }
    };

    /**
     * 云端调用设备的某项服务的时候，设备端需要响应该服务并回复。
     * 设备端事件触发的时候需要调用这个接口上报事件，如事件告警等
     * 需要用户在云端定义不同的 Error 的类型
     */
    private void setServiceHandler() {
        Log.d(TAG, "setServiceHandler() called");
        IThing thing = null;
        if (!isSubDev) {
            thing = LinkKit.getInstance().getDeviceThing();
        } else {
            thing = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first;
        }
        if (thing == null){
            showToast("设备当前状态不支持");
            return;
        }

        List<Service> srviceList = thing.getServices();
        for (int i = 0; srviceList != null && i < srviceList.size(); i++) {
            Service service = srviceList.get(i);
            if (!isSubDev) {
                thing.setServiceHandler(service.getIdentifier(), resRequestHandler);
            } else{
                thing.setServiceHandler(service.getIdentifier(), resRequestHandler);
            }
        }
        //
    }

    static class ResRequestHandler implements ITResRequestHandler{
        WeakReference<ITResRequestHandler> handlerWakRef = null;

        public ResRequestHandler(ITResRequestHandler handler) {
            handlerWakRef = new WeakReference<>(handler);
        }

        @Override
        public void onProcess(String s, Object o, ITResResponseCallback itResResponseCallback) {
            if (handlerWakRef != null && handlerWakRef.get() != null) {
                handlerWakRef.get().onProcess(s, o, itResResponseCallback);
            }
        }

        @Override
        public void onSuccess(Object o, OutputParams outputParams) {
            if (handlerWakRef != null && handlerWakRef.get() != null) {
                handlerWakRef.get().onSuccess(o, outputParams);
            }
        }

        @Override
        public void onFail(Object o, ErrorInfo errorInfo) {
            if (handlerWakRef != null && handlerWakRef.get() != null) {
                handlerWakRef.get().onFail(o, errorInfo);
            }
        }
    }

    private ITResRequestHandler mCommonHandler = new ITResRequestHandler() {
        @Override
        public void onProcess(String identify, Object result, ITResResponseCallback itResResponseCallback) {
            Log.d(TAG, "onProcess() called with: s = [" + identify + "], o = [" + result + "], itResResponseCallback = [" + itResResponseCallback + "]");
            showToast("收到异步服务调用 " + identify);
            try {
                if (SERVICE_SET.equals(identify)) {
                    // TODO  用户按照真实设备的接口调用  设置设备的属性
                    // 设置完真实设备属性之后，上报设置完成的属性值
                    // 用户根据实际情况判断属性是否设置成功 这里测试直接返回成功
                    boolean isSetPropertySuccess = true;
                    if (isSetPropertySuccess){
                        if (result instanceof InputParams) {
                            Map<String, ValueWrapper> data = (Map<String, ValueWrapper>) ((InputParams) result).getData();
//                        data.get()

                            // 响应云端 接收数据成功
                            itResResponseCallback.onComplete(identify, null, null);
                        } else {
                            itResResponseCallback.onComplete(identify, null, null);
                        }
                        updatePropertyValue((Property) mPropertySpinner.getSelectedItem());
                    } else {
                        AError error = new AError();
                        error.setCode(100);
                        error.setMsg("setPropertyFailed.");
                        itResResponseCallback.onComplete(identify, new ErrorInfo(error), null);
                    }

                } else if (SERVICE_GET.equals(identify)){
                    //  初始化的时候将默认值初始化传进来，物模型内部会直接返回云端缓存的值

                } else {
                    // 根据不同的服务做不同的处理，跟具体的服务有关系
                    showToast("用户根据真实的服务返回服务的值，请参照set示例");
                    OutputParams outputParams = new OutputParams();
//                    outputParams.put("op", new ValueWrapper.IntValueWrapper(20));
                    itResResponseCallback.onComplete(identify,null, outputParams);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("TMP 返回数据格式异常");
            }
        }

        @Override
        public void onSuccess(Object o, OutputParams outputParams) {
            Log.d(TAG, "onSuccess() called with: o = [" + o + "], outputParams = [" + outputParams + "]");
            showToast("注册服务成功");
        }

        @Override
        public void onFail(Object o, ErrorInfo errorInfo) {
            Log.d(TAG, "onFail() called with: o = [" + o + "], errorInfo = [" + errorInfo + "]");
            showToast("注册服务失败");
        }
    };

    private ResRequestHandler resRequestHandler = new ResRequestHandler(mCommonHandler);

    private void updatePropertyValue(final Property property) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (property != null && property.equals(mPropertySpinner.getSelectedItem())) {
                    ValueWrapper valueWrapper = null;
                    if (!isSubDev) {
                        valueWrapper = LinkKit.getInstance().getDeviceThing().getPropertyValue(property.getIdentifier());
                    } else {
                        valueWrapper = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first.getPropertyValue(property.getIdentifier());
                    }
                    if (valueWrapper != null && valueWrapper.getValue() != null) {
                        String showVaue = String.valueOf(valueWrapper.getValue());
                        if (valueWrapper.getValue() instanceof List){
                            StringBuffer sb = new StringBuffer();
                            sb.append("[");
                            for (int i = 0; i < ((List<ValueWrapper>) valueWrapper.getValue()).size(); i++) {
                                ValueWrapper item = ((List<ValueWrapper>) valueWrapper.getValue()).get(i);
                                if ("string".equals(item.getType())){
                                    sb.append("\"").append(item.getValue()).append("\"");
                                } else {
                                    sb.append(item.getValue());
                                }
                                if (i != ((List) valueWrapper.getValue()).size() - 1){
                                    sb.append(",");
                                }
                            }
                            sb.append("]");
                            showVaue = sb.toString();
                        } else if (valueWrapper.getValue() instanceof Map){
                            StringBuffer sb = new StringBuffer();
                            sb.append("{");
                            for (Map.Entry<String, ValueWrapper> entry: ((Map<String, ValueWrapper>) valueWrapper.getValue()).entrySet()){
                                sb.append("\"").append(entry.getKey()).append("\"").append(":");
                                if ("string".equals(entry.getValue().getType())){
                                    sb.append("\"").append(entry.getValue().getValue()).append("\"");
                                } else {
                                    sb.append(entry.getValue().getValue());
                                }
                                sb.append(",");
                            }
                            if (sb.length() > 1){
                                sb.deleteCharAt(sb.length()-1);
                            }
                            sb.append("}");
                            showVaue = sb.toString();
                        }
                        mPropertyET.setText(showVaue);
                    } else {
                        mPropertyET.setText("");
                    }
                }
            }
        });
    }

    public void postProperty(View view) {
        try {
            Property property = (Property) mPropertySpinner.getSelectedItem();
            if (property == null) {
                showToast("选择的property为空");
                return;
            }
            String value = mPropertyET.getText().toString();
            if (property == null || value == null || property.getIdentifier() == null) {
                showToast("属性或值为空");
                return;
            }
            if (property.getDataType() == null) {
                showToast("属性类型为空");
                return;
            }
//        if (true){
//            ValueWrapper valueWrapper = GsonUtils.fromJson(value, new TypeToken<ValueWrapper>(){}.getType());
//            report(property.getIdentifier(), valueWrapper);
//            return;
//        }
            if (TmpConstant.TYPE_VALUE_INTEGER.equals(property.getDataType().getType())) {
                int parseData = getInt(value);
                if (parseData != DEF_VALUE) {
                    report(property.getIdentifier(), new ValueWrapper.IntValueWrapper(parseData));
                } else {
                    showToast("数据格式不对");
                }
                return;
            }
            if (TmpConstant.TYPE_VALUE_FLOAT.equals(property.getDataType().getType())) {
                Double parseData = getDouble(value);
                if (parseData != null) {
                    report(property.getIdentifier(), new ValueWrapper.DoubleValueWrapper(parseData));
                } else {
                    showToast("数据格式不对");
                }
                return;
            }
            if (TmpConstant.TYPE_VALUE_DOUBLE.equals(property.getDataType().getType())) {
                Double parseData = getDouble(value);
                if (parseData != null) {
                    report(property.getIdentifier(), new ValueWrapper.DoubleValueWrapper(parseData));
                } else {
                    showToast("数据格式不对");
                }
                return;
            }
            if (TmpConstant.TYPE_VALUE_BOOLEAN.equals(property.getDataType().getType())) {
                int parseData = getInt(value);
                if (parseData == 0 || parseData == 1) {
                    report(property.getIdentifier(), new ValueWrapper.BooleanValueWrapper(parseData));
                } else {
                    showToast("数据格式不对");
                }
                return;
            }
            if (TmpConstant.TYPE_VALUE_TEXT.equals(property.getDataType().getType())) {
                report(property.getIdentifier(), new ValueWrapper.StringValueWrapper(value));
                return;
            }
            if (TmpConstant.TYPE_VALUE_DATE.equals(property.getDataType().getType())) {
                report(property.getIdentifier(), new ValueWrapper.DateValueWrapper(value));
                return;
            }
            if(TmpConstant.TYPE_VALUE_ENUM.equalsIgnoreCase(property.getDataType().getType())){
                report(property.getIdentifier(),new ValueWrapper.EnumValueWrapper(getInt(value)));
                return;
            }
            if(TmpConstant.TYPE_VALUE_ARRAY.equalsIgnoreCase(property.getDataType().getType())){
                ValueWrapper.ArrayValueWrapper arrayValueWrapper = GsonUtils.fromJson(value,new TypeToken<ValueWrapper>(){}.getType());
                report(property.getIdentifier(),arrayValueWrapper);
                return;
            }
            // 结构体数据解析  结构体不支持嵌套结构体和数组
            if (TmpConstant.TYPE_VALUE_STRUCT.equals(property.getDataType().getType())) {
                try {
                    List<Map<String, Object>> specsList = (List<Map<String, Object>>) property.getDataType().getSpecs();
                    if (specsList == null ||specsList.size() == 0){
                        showToast("云端创建的struct结构为空，不上传任何值。");
                        return;
                    }
                    JSONObject dataJson = JSONObject.parseObject(value);
                    Map<String, ValueWrapper> dataMap = new HashMap<>();
                    Map<String, Object> specsItem = null;
                    for (int i = 0; i < specsList.size(); i++) {
                        specsItem = specsList.get(i);
                        if (specsItem == null){
                            continue;
                        }
                        String idKey = (String) specsItem.get("identifier");
                        String dataType = (String) ((Map)specsItem.get("dataType")).get("type");
                        if (idKey != null && dataJson.containsKey(idKey) && dataType != null){
                            ValueWrapper valueItem = null;
                            if ("int".equals(dataType)){
                                valueItem = new ValueWrapper.IntValueWrapper(getInt(String.valueOf(dataJson.get(idKey))));
                            } else if ("text".equals(dataType)){
                                valueItem = new ValueWrapper.StringValueWrapper((String) dataJson.get(idKey));
                            } else if ("float".equals(dataType) || "double".equals(dataType)){
                                valueItem = new ValueWrapper.DoubleValueWrapper(getDouble(String.valueOf(dataJson.get(idKey))));
                            } else if ("bool".equals(dataType)){
                                valueItem = new ValueWrapper.BooleanValueWrapper(getInt(String.valueOf(dataJson.get(idKey))));
                            } else if ("date".equals(dataType)){
                                if (isValidInt(String.valueOf(dataJson.get(idKey)))) {
                                    valueItem = new ValueWrapper.DateValueWrapper(String.valueOf(dataJson.get(idKey)));
                                } else {
                                    showToast("数据格式不对");
                                }
                            } else if ("enum".equals(dataType)){
                                valueItem = new ValueWrapper.EnumValueWrapper(getInt(String.valueOf(dataJson.get(idKey))));
                            } else {
                                showToast("数据格式不支持");
                            }
                            if (valueItem != null) {
                                dataMap.put(idKey, valueItem);
                            }
                        }
                    }

                    report(property.getIdentifier(), new ValueWrapper.StructValueWrapper(dataMap));
                } catch (Exception e){
                    showToast("数据格式不正确");
                }
                return;
            }
            showToast("该类型Demo暂不支持，用户可参照其他类型代码示例开发支持。");
        } catch (Exception e) {
            showToast("数据格式不对");
            e.printStackTrace();
        }
    }

    public void postEvent(View view) {
        Event event = (Event) mEventSpinner.getSelectedItem();
        if (event == null) {
            showToast("选择的时间为空");
            return;
        }

        HashMap<String, ValueWrapper> hashMap = new HashMap<>();
        try {
            String mapEventData = mEventET.getText().toString();
            JSONObject object = JSONObject.parseObject(mapEventData);
            if (object == null){
                showToast("参数不能为空");
                return;
            }
            if (event.getOutputData() != null) {
                for (int i = 0; i < event.getOutputData().size(); i++) {
                    Arg arg = event.getOutputData().get(i);
                    if (arg == null || arg.getDataType() == null || arg.getIdentifier() == null){
                        continue;
                    }
                    String idnValue = String.valueOf(object.get(arg.getIdentifier()));
                    if (idnValue == null || object.get(arg.getIdentifier()) == null){
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_INTEGER.equals(arg.getDataType().getType())) {
                        int parseData = getInt(idnValue);
                        if (parseData != DEF_VALUE) {
                            hashMap.put(arg.getIdentifier(), new ValueWrapper.IntValueWrapper(parseData));
                        } else {
                            showToast("数据格式不对");
                            break;
                        }
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_FLOAT.equals(arg.getDataType().getType())) {
                        Double parseData = getDouble(idnValue);
                        if (parseData != null) {
                            hashMap.put(arg.getIdentifier(), new ValueWrapper.DoubleValueWrapper(parseData));
                        } else {
                            showToast("数据格式不对");
                            break;
                        }
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_DOUBLE.equals(arg.getDataType().getType())) {
                        Double parseData = getDouble(idnValue);
                        if (parseData != null) {
                            hashMap.put(arg.getIdentifier(), new ValueWrapper.DoubleValueWrapper(parseData));
                        } else {
                            showToast("数据格式不对");
                            break;
                        }
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_BOOLEAN.equals(arg.getDataType().getType())) {
                        int parseData = getInt(idnValue);
                        if (parseData == 0 || parseData == 1) {
                            hashMap.put(arg.getIdentifier(), new ValueWrapper.BooleanValueWrapper(parseData));
                        } else {
                            showToast("数据格式不对");
                            break;
                        }
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_TEXT.equals(arg.getDataType().getType())) {
                        hashMap.put(arg.getIdentifier(), new ValueWrapper.StringValueWrapper(idnValue));
                        continue;
                    }
                    if (TmpConstant.TYPE_VALUE_DATE.equals(arg.getDataType().getType())) {
                        hashMap.put(arg.getIdentifier(), new ValueWrapper.DateValueWrapper(idnValue));
                        continue;
                    }
                    if(TmpConstant.TYPE_VALUE_ENUM.equalsIgnoreCase(arg.getDataType().getType())){
                        hashMap.put(arg.getIdentifier(),new ValueWrapper.EnumValueWrapper(getInt(idnValue)));
                        continue;
                    }
                    if(TmpConstant.TYPE_VALUE_ARRAY.equalsIgnoreCase(arg.getDataType().getType())){
                        ValueWrapper.ArrayValueWrapper arrayValueWrapper = GsonUtils.fromJson(idnValue,new TypeToken<ValueWrapper>(){}.getType());
                        hashMap.put(arg.getIdentifier(),arrayValueWrapper);
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("数据格式错误");
            return;
        }
        try {
            OutputParams params = new OutputParams(hashMap);
            if (!isSubDev) {
                LinkKit.getInstance().getDeviceThing().thingEventPost(event.getIdentifier(), params, resourceListener);
            } else {
                IThing thing = LinkKit.getInstance().getGateway().getSubDeviceThing(mBaseInfo).first;
                if (thing == null){
                    showToast("子设备当前状态不支持");
                    return;
                }
                thing.thingEventPost(event.getIdentifier(), params, resourceListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("上报失败 " + e);
        }
    }

    public void thingRawPropertiesPost() {
        try {
            byte[] rawData = {0x01,0x02,0x03,0x04,0x05,0x06,0x07};

            LinkKit.getInstance().getDeviceThing().thingRawPropertiesPost(rawData, devRawDataListener);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("上报失败 " + e);
        }
    }

    private static IDevRawDataListener devRawDataListener =  new IDevRawDataListener() {
        @Override
        public void onSuccess(Object o, Object o1) {
            Log.d(TAG, "onSuccess() called with: s = [" + o1 + "], o = [" + o + "]");
            showToast("数据上行成功，业务处理是否成功参见云端返回数据");
        }

        @Override
        public void onFail(Object o, ErrorInfo errorInfo) {
            Log.d(TAG, "onError() called with: s = [" + o + "], aError = [" + errorInfo + "]");
            showToast("设备上报状态失败");
        }
    };
}
