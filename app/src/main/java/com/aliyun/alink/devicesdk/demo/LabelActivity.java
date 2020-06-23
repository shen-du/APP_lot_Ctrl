package com.aliyun.alink.devicesdk.demo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.alink.dm.model.RequestModel;
import com.aliyun.alink.linkkit.api.LinkKit;

import java.util.List;
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

public class LabelActivity extends BaseTemplateActivity {
    private static final String TAG = "LabelActivity";

    private String updateLabel = "{" + "  \"id\": \"123\"," + "  \"version\": \"1.0\"," +
            "  \"params\": [" + "    {" + "      \"attrKey\": \"Temperature\"," +
            "      \"attrValue\": \"36.8\"" + "    }" + "  ]," +
            "  \"method\": \"thing.deviceinfo.update\"" + "}";

    private String deleteLabel = "{" + "  \"id\": \"123\"," + "  \"version\": \"1.0\"," +
            "  \"params\": [" + "    {" + "      \"attrKey\": \"Temperature\"" +
            "    }" + "  ]," + "  \"method\": \"thing.deviceinfo.delete\"" + "}";

    @Override
    protected void initViewData() {
        funcTV1.setText("更新标签");
        funcBT1.setText("更新");
        funcET1.setText(updateLabel);

        funcTV2.setText("删除标签");
        funcBT2.setText("删除");
        funcET2.setText(deleteLabel);
    }

    @Override
    protected void onFunc2Click() {
        try {
            String labelsData = funcET2.getText().toString();
            RequestModel<List<Map>> requestModel = JSONObject.parseObject(labelsData, new TypeReference<RequestModel<List<Map>>>() {
            }.getType());
            LinkKit.getInstance().getDeviceLabel().labelDelete(requestModel, BaseTemplateActivity.mConnectSendListener);
        } catch (Exception e) {
            showToast("数据格式不对");
            e.printStackTrace();
        }
    }

    @Override
    protected void onFunc1Click() {
        try {
            String labelsData = funcET1.getText().toString();
            RequestModel<List<Map>> requestModel = JSONObject.parseObject(labelsData, new TypeReference<RequestModel<List<Map>>>() {
            }.getType());
            LinkKit.getInstance().getDeviceLabel().labelUpdate(requestModel, BaseTemplateActivity.mConnectSendListener);
        } catch (Exception e) {
            showToast("数据格式不对");
            e.printStackTrace();
        }
    }
}
