package com.aliyun.alink.devicesdk.demo;

import android.os.Process;
import android.view.View;

import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.cmp.core.base.ARequest;
import com.aliyun.alink.linksdk.cmp.core.base.AResponse;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectSendListener;
import com.aliyun.alink.linksdk.tools.AError;

/**
 * author : Jeeking
 * date   : 2020-02-19 14:07
 * desc   : 设备重置，会解除和应用的绑定关系，并deinit设备
 */
public class ResetActivity extends BaseTemplateActivity {
    @Override
    protected void initViewData() {
        funcTV1.setText("reset");
        funcET1.setEnabled(false);
        /**
         * （1）如果设备在线，调用reset接口到云端，成功之后销毁整个linkkit，失败按照流程2处理；
         * （2）如果设备不在线，设置一个reset的标志位，然后销毁整个linkkit，后续linkkit被重新初始化的时候，根据是否有这个reset标志位在建联成功之后再调用reset接口，reset完成之后才能对外发布token；
         */
        funcBT1.setText("设备重置");

        funcRL2.setVisibility(View.GONE);
    }

    @Override
    protected void onFunc1Click() {
        LinkKit.getInstance().reset(new IConnectSendListener() {
            @Override
            public void onResponse(ARequest aRequest, AResponse aResponse) {
                waitAndKill();
            }

            @Override
            public void onFailure(ARequest aRequest, AError aError) {
                waitAndKill();
            }
        });
    }

    @Override
    protected void onFunc2Click() {

    }

    private void waitAndKill() {
        showToast("设备重置完成，即将退出应用");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        killProcess();
    }

    private void killProcess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Process.killProcess(Process.myPid());
            }
        });
    }
}
