package com.aliyun.alink.devicesdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aliyun.alink.devicesdk.demo.R;
import com.aliyun.alink.linksdk.tmp.devicemodel.Event;

import java.util.ArrayList;
import java.util.List;

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

public class EventListAdapter extends BaseAdapter {
    private List<Event> propertyList = null;
    private LayoutInflater layoutInflater = null;

    public EventListAdapter(Context context) {
        this.propertyList = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }

    public void setListData(List<Event> propertyList) {
        this.propertyList.clear();
        if (propertyList != null) {
            this.propertyList.addAll(propertyList);
        }
    }

    @Override
    public int getCount() {
        return propertyList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position > -1 && position < propertyList.size()) {
            return propertyList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.listview_item_property, null);
            holder.value = convertView.findViewById(R.id.propertyKey);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Event event = (Event) getItem(position);
        if (event != null){
            holder.value.setText(event.getName() + event.getType());
        }
        return convertView;
    }

    private final class ViewHolder {
        private TextView value;
    }
}
