package com.aliyun.alink.devicesdk.demo;

import android.text.TextUtils;
import android.util.Log;

import com.aliyun.alink.devicesdk.manager.MD5Util;
import com.aliyun.alink.h2.api.CompletableListener;
import com.aliyun.alink.h2.api.StreamWriteContext;
import com.aliyun.alink.h2.entity.Http2Request;
import com.aliyun.alink.h2.entity.Http2Response;
import com.aliyun.alink.h2.stream.api.CompletableDataListener;
import com.aliyun.alink.h2.stream.api.IDownStreamListener;
import com.aliyun.alink.h2.stream.api.IStreamSender;
import com.aliyun.alink.h2.stream.utils.StreamUtil;
import com.aliyun.alink.linkkit.api.LinkKit;
import com.aliyun.alink.linksdk.tools.ALog;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.handler.codec.http2.Http2Headers;

public abstract class BaseH2TestActivity extends BaseTemplateActivity {
    private static final String TAG = "BaseH2TestActivity";
    IStreamSender client = null;

    private String dataStreamId = null;
    private String fileUploadId = null;
    protected int unlimitedStreamId = 0;
    private AtomicBoolean dataStreamOpened = new AtomicBoolean(false);
    protected final static int TYPE_REQUEST = 0;
    protected final static int TYPE_UNLIMITED_REQUEST = 1;
    protected final static int TYPE_UNLIMITED_REQUEST_END = 2;
    protected final static int TYPE_DOWNSTREAM_REQUEST = 3;

    protected void connect(final CompletableListener listener) {

        // 线上版本 保持和设备三元组一致
        /**
         * 设备认证方式 初始化放在LinkKit 初始化里面
         * 这里使用的时候直接获取
         */
        client = LinkKit.getInstance().getH2StreamClient();

        try {
            client.connect(new CompletableListener<Object>() {

                @Override
                public void complete(Object o) {
                    if (o instanceof Throwable) {
                        listener.completeExceptionally((Throwable) o);
                        showToast("connect failed " + ((Throwable) o).getMessage());
                        return;
                    }
                    if (listener != null) {
                        showToast("connect success");
                        listener.complete(o);
                    }
                }

                @Override
                public void completeExceptionally(Throwable throwable) {
                    showToast("connect fail " + (throwable == null ? "" : throwable.getMessage()));
                    if (listener != null) {
                        listener.completeExceptionally(throwable);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void openStream(final String serviceNme) {
        if (client == null || !client.isConnected()) {
            showToast("connect first.");
            dataStreamOpened.set(false);
            return;
        }
//        if (dataStreamOpened.get()) {
//            showToast("stream opened.");
//            return;
//        }

        ALog.i(TAG, "open stream");
        final Http2Request request = new Http2Request();
        request.getHeaders().add("x-for-test", "ddd");
//        request.getHeaders().add("x-file-name", "tesxx.jpg");
//        request.getHeaders().add("x-file-overwrite", "1");
//        request.getHeaders().add("x-file-content-type", "ddd");
        client.openStream(serviceNme,
                request, new CompletableListener<Http2Response>() {
                    @Override
                    public void complete(Http2Response http2Response) {
                        ALog.w(TAG, "open stream result: " + http2Response);
                        showToast("open Stream success");
                        if (http2Response != null) {
                            dataStreamId = StreamUtil.getDataStreamId(http2Response.getHeaders());
                            ALog.d(TAG, "dataStreamId=" + dataStreamId);
                            dataStreamOpened.set(true);

//                            if (http2Response.getHeaders() != null) {
//                                fileUploadId = (String) http2Response.getHeaders().get("x-file-upload-id", "").toString();
//                            }
                        }
                    }

                    @Override
                    public void completeExceptionally(Throwable throwable) {
                        showToast("open Stream failed");
                        Log.w(TAG, "completeExceptionally: ", throwable);
                        dataStreamOpened.set(false);
                    }
                });
    }

    /**
     * 文件上传
     *
     * @param serviceNme
     */
    protected void uploadFile(final String serviceNme, String filePath) {
        if (client == null || !client.isConnected()) {
            showToast("connect first.");
            dataStreamOpened.set(false);
            return;
        }

        if (serviceNme == null || serviceNme.isEmpty() || filePath == null || filePath.isEmpty()) {
            showToast("serviceNme or filePath is null or empty");
            return;
        }

        final Http2Request request = new Http2Request();
        request.getHeaders().add("x-file-name", getFileName(filePath));
        request.getHeaders().add("x-file-overwrite", "1");//是否覆盖
//        request.getHeaders().add("x-file-content-type", "ddd");//
        client.uploadFile(serviceNme, request, filePath, new CompletableDataListener<Http2Response>() {
            @Override
            public void complete(Http2Response http2Response) {
                showToast(http2Response.toString() + "success");
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                showToast(throwable.toString() + "fail");
            }

            @Override
            public void callBack(String fileUploadID) {
                fileUploadId = fileUploadID;
            }
        });
    }


    /**
     * 文件上传-断点续传
     *
     * @param serviceNme
     */
    protected void renewalFile(final String serviceNme, String filePath) {
        if (client == null || !client.isConnected()) {
            showToast("connect first.");
            dataStreamOpened.set(false);
            return;
        }

        if (serviceNme == null || serviceNme.isEmpty() || filePath == null || filePath.isEmpty()) {
            showToast("serviceNme or filePath is null or empty");
            return;
        }

        final Http2Request request = new Http2Request();
        request.getHeaders().add("x-file-upload-id", fileUploadId);//
        client.uploadFile(serviceNme, request, filePath, new CompletableDataListener<Http2Response>() {
            @Override
            public void complete(Http2Response http2Response) {
                showToast(http2Response.toString() + "success");
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                showToast(throwable.toString() + "fail");
            }

            @Override
            public void callBack(String fileUploadID) {
                fileUploadId = fileUploadID;
            }
        });
    }


    protected void sendStreamData(String data, final int type, int length, final int streamId) {
        ALog.i(TAG, "sendStreamData() called with: data = [" + data + "], type = [" + type + "], streamId = [" + streamId + "]");
        if (client == null || !client.isConnected()) {
            showToast("connect first.");
            dataStreamOpened.set(false);
            return;
        }
        if (client == null || dataStreamOpened.compareAndSet(false, false)) {
            showToast("open Stream first.");
            return;
        }
        if (TextUtils.isEmpty(data)) {
            showToast("data cannot be null.");
            return;
        }
        String sendData = data;
        Http2Request request = new Http2Request();
        request.setContent(sendData.getBytes());
        boolean endOfStream = type == TYPE_REQUEST || type == TYPE_DOWNSTREAM_REQUEST;
        request.setEndOfStream(endOfStream);
        request.getHeaders().add("x-for-test", "ddd");
//        if (!TextUtils.isEmpty(fileUploadId)) {
//            request.getHeaders().add("x-file-upload-id", fileUploadId);
//        }
        request.getHeaders().set("content-length", String.valueOf(length));
        if (type == TYPE_UNLIMITED_REQUEST || type == TYPE_UNLIMITED_REQUEST_END) {
            ALog.d(TAG, "发送无限流");
            request.setEndOfStream(type == TYPE_UNLIMITED_REQUEST_END);
            if (streamId != 0) {
                request.setH2StreamId(streamId);
            }
        } else if (type == TYPE_DOWNSTREAM_REQUEST) {
            ALog.d(TAG, "发送下推流请求");
            request.getHeaders().add("x-test-downstream", "随便填");
        } else if (type == TYPE_REQUEST) {
            ALog.d(TAG, "发送有限流请求");
        } else {
            ALog.e(TAG, "request type error.");
            return;
        }
        client.sendStream(dataStreamId, request,
                new IDownStreamListener() {
                    @Override
                    public void onHeadersRead(String s, Http2Headers http2Headers, boolean b) {
                        ALog.d(TAG, "onHeadersRead() called with: s = [" + s + "], http2Headers = [" + http2Headers + "], b = [" + b + "]");
                        unlimitedStreamId = 0;
                        showToast("receive headers=" + http2Headers);
                    }

                    @Override
                    public void onDataRead(String s, byte[] bytes, boolean b) {
                        unlimitedStreamId = 0;
                        ALog.d(TAG, "onDataRead() called with: s = [" + s + "], bytes = [" + new String(bytes) + "], b = [" + b + "]");
                    }

                    @Override
                    public void onStreamError(String s, IOException e) {
                        unlimitedStreamId = 0;
                        ALog.w(TAG, "onStreamError() called with: s = [" + s + "], e = [" + e + "]");
                    }
                }, new CompletableListener<StreamWriteContext>() {
                    @Override
                    public void complete(StreamWriteContext streamWriteContext) {
                        ALog.d(TAG, "complete() called with: streamWriteContext = [" + streamWriteContext + "]");
                        if (type == TYPE_UNLIMITED_REQUEST && streamWriteContext != null) {
                            unlimitedStreamId = streamWriteContext.getStream().id();
                            ALog.i(TAG, "unlimitedStreamId=" + unlimitedStreamId);
                        }
                    }

                    @Override
                    public void completeExceptionally(Throwable throwable) {
                        unlimitedStreamId = 0;
                        ALog.w(TAG, "completeExceptionally() called with: throwable = [" + throwable + "]");
                    }
                });
    }

    protected void sendFile(String filePath) {
        if (client == null) {
            showToast("open stream first.");
            return;
        }
        if (!client.isConnected()) {
            showToast("connect first.");
            dataStreamOpened.set(false);
            return;
        }
//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.test);
        if (TextUtils.isEmpty(filePath)) {
            showToast("filepath empty");
            return;
        }
        ALog.d(TAG, "filePath = " + filePath);
        Http2Request request = new Http2Request();
        request.getHeaders().add("x-for-test", "ddd");
        String md5 = getFileMd5(filePath);
        if (!TextUtils.isEmpty(md5)) {
            ALog.d(TAG, "fileMd5=" + md5);
            request.getHeaders().add("x-send-md5", md5);
        }
        String filename = null;
        try {
            if (filePath.contains("/")) {
                filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
            } else {
                filename = filePath;
            }
            if (filename != null) {
                ALog.d(TAG, "filename=" + filename);
                request.getHeaders().add("filename", filename);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("filepath invalid");
            return;
        }
//        client.upload(dataStreamId, filePath, request, new CompletableListener<Http2Response>() {
//            @Override
//            public void complete(Http2Response o) {
//                ALog.d(TAG, "complete() called with: o = [" + o + "]");
//                showToast("upload success");
//            }
//
//            @Override
//            public void completeExceptionally(Throwable throwable) {
//                ALog.w(TAG, "completeExceptionally() called with: throwable = [" + throwable + "]");
//                showToast("upload fail");
//            }
//        });
    }

    private String getFileMd5(String filePath) {
        return MD5Util.getFileMd5(filePath);
    }

    protected void closeStream() {
        ALog.i(TAG, "close stream");
        if (client == null || dataStreamOpened.compareAndSet(false, false)) {
            showToast("close success.");
            return;
        }
        Http2Request request = new Http2Request();
        request.setEndOfStream(true);
        request.getHeaders().add("x-for-test", "ddd");
        client.closeStream(dataStreamId, request, new CompletableListener<Http2Response>() {
            @Override
            public void complete(Http2Response http2Response) {
                ALog.d(TAG, "complete() called with: http2Response = [" + http2Response + "]");
                showToast("close stream success");
                dataStreamId = null;
                unlimitedStreamId = 0;
                dataStreamOpened.set(false);
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                ALog.d(TAG, "completeExceptionally() called with: throwable = [" + throwable + "]");
                showToast("close stream exception");
                dataStreamId = null;
                dataStreamOpened.set(false);
                unlimitedStreamId = 0;
            }
        });
    }

    protected void disconnect(final CompletableListener listener) {
        ALog.d(TAG, "disconnect");
        dataStreamOpened.set(false);
        if (client != null) {
            client.disconnect(new CompletableListener<Object>() {

                @Override
                public void complete(Object o) {
                    disconnected();
                    if (listener != null) {
                        listener.complete(o);
                    }
                }

                @Override
                public void completeExceptionally(Throwable throwable) {
                    disconnected();
                    if (listener != null) {
                        listener.completeExceptionally(throwable);
                    }
                }
            });
        }

    }

    protected void disconnected() {
        showToast("disconnect success");
        unlimitedStreamId = 0;
        dataStreamId = null;
        dataStreamOpened.set(false);
        client = null;
    }

    public String getFileName(String filePath) {
        try {
            int start = filePath.lastIndexOf("/");
//        int end = filePath.lastIndexOf(".");
            if (start != -1) {
                return filePath.substring(start + 1);
            } else {
                return "unKnow";
            }
        } catch (Exception e){
            return "unKnow";
        }
    }
}
