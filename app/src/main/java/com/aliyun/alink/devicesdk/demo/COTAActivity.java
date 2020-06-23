package com.aliyun.alink.devicesdk.demo;

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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.linkkit.api.LinkKit;

import java.util.Map;

public class COTAActivity extends BaseTemplateActivity {
    private static final String TAG = "COTAActivity";

    private String cOTAGet = "{" + "  \"id\": 123," + "  \"version\": \"1.0\"," +
            "  \"params\": {" + "\"configScope\": \"product\"," + "\"getType\": \"file\"" +
            "  }," + "  \"method\": \"thing.config.get\"" + "}";

    @Override
    protected void initViewData() {
        funcTV1.setText("获取远程配置");
        funcBT1.setText("获取");
        funcET1.setText(cOTAGet);

        funcTV2.setText("监听远程配置下行");
        funcBT2.setText("监听");
    }

    @Override
    protected void onFunc2Click() {
        LinkKit.getInstance().getDeviceCOTA().setCOTAChangeListener(mConnectRrpcListener);
    }

    @Override
    protected void onFunc1Click() {
        try {
            String getData = funcET1.getText().toString();
            RequestModel<Map> requestModel = JSONObject.parseObject(getData, new TypeReference<RequestModel<Map>>() {
            }.getType());
            LinkKit.getInstance().getDeviceCOTA().COTAGet(requestModel, mConnectSendListener);
        } catch (Exception e) {
            showToast("数据格式不对");
            e.printStackTrace();
        }
    }


}
