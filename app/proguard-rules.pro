# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ----不提示未找到的类----
#-dontnote

# ----不做混淆优化----
-dontoptimize

# ----混淆时不会产生形形色色的类名----
-dontusemixedcaseclassnames

# ----指定不忽略非公共的库类----
-dontskipnonpubliclibraryclasses

# ----混淆包路径----
-repackageclasses ''

# ----不预校验----
-dontpreverify

# ----输出生成信息----
-verbose

#-keepattributes MethodParameters
# ----优化时允许访问并修改有修饰符的类和类的成员----
-allowaccessmodification

# ----保护类型----
-keepattributes *Annotation*,SourceFile,Signature,LineNumberTable,InnerClasses

# ----打印混淆信息----
-printmapping map.txt

-keepattributes InnerClasses

# ----保留R文件----
-keep class **.R
-keep class **.R$* {
    <fields>;
}


#-------------------
# 打包时忽略以下类的警告
#-------------------
-dontwarn java.awt.**
-dontwarn android.test.**
-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }
-dontwarn android.support.v7.**
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }

-keep class android.**{*;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application # 保持哪些类不被混淆
-keep public class * extends android.app.Service # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep class * implements android.os.Parcelable {#保持Parcelable不被混淆
   public static final android.os.Parcelable$Creator *;
}

# Java
-keep class * implements java.io.Serializable{*;}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    <fields>;
    **[] $VALUES;
}

-keepattributes Exceptions

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# linkkit API
-keep class com.aliyun.alink.**{*;}
-keep class com.aliyun.linksdk.**{*;}
-dontwarn com.aliyun.**
-dontwarn com.alibaba.**
-dontwarn com.alipay.**
-dontwarn com.ut.**

# keep native method
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep netty
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** {
    *;
}
-keepnames class io.netty.** {
    *;
}
-dontwarn io.netty.**
-dontwarn sun.**

# keep mqtt
-keep public class org.eclipse.paho.**{*;}

# keep fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*;}

# keep gson
-keep class com.google.gson.** { *;}

# keep network core
-keep class com.http.**{*;}
-keep class org.mozilla.**{*;}

# keep okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.mozilla.**

-keep class okio.**{*;}
-keep class okhttp3.**{*;}
-keep class org.apache.commons.codec.**{*;}

-keep class com.aliyun.alink.devicesdk.demo.FileProvider{*;}
-keep class android.support.**{*;}
-keep class android.os.**{*;}