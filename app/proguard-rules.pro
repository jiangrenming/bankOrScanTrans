# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in F:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
#-------------------------------------------定制化区域----------------------------------------------
#---------------------------------1.实体类---------------------------------
#------------------------扫码library相关实体类-----------------------------#
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,EnclosingMethod
-keep class com.nld.starpos.wxtrade.bean.** { *; }
-keep class com.nld.starpos.wxtrade.http.ApiTools{ *; }
-keepnames class com.nld.netlibrary.xutils.AsyncRequestCallBack$* {
    public <fields>;
    public <methods>;
}
-keep class com.nld.starpos.wxtrade.local.db.bean.** { *; }
-keep class com.nld.starpos.wxtrade.local.db.ScanParamsDao{ *; }
-keep class com.nld.starpos.wxtrade.local.db.ScanTransDao{ *; }
-keep class com.nld.starpos.wxtrade.local.db.imp.** { *; }
-keep class com.nld.starpos.wxtrade.thread.scan_thread.QCScanPayThread{ *; }
-keep class com.nld.starpos.wxtrade.thread.ComonThread{ *; }
-keep class com.nld.starpos.wxtrade.utils.params.TransType{ *; }
#------------------------主项目相关实体类-----------------------------#
-keep public class com.nld.cloudpos.wechat.thread.ComonThread{*;}
-keep public class com.nld.cloudpos.payment.dev.PrintDev{*;}
-keep public class com.nld.starpos.banktrade.db.** { *; }
-keep public class common.Utility{*;}
-keep public class com.nld.starpos.banktrade.thread.ComonThread{*;}
-keep public class com.nld.starpos.banktrade.thread.BankConsumeThread{*;}
-keep public class com.nld.netlibrary.https.HttpConnetionHelper{*;}
-keep public class com.nld.netlibrary.https.HttpClient{*;}
-keep public class com.nld.netlibrary.https.BankConfigBuilder{*;}
-keep public class com.nld.starpos.banktrade.thread.OfflineUploadThread{
  private *;
}

#-------------------------------------------------------------------------

#---------------------------------2.第三方包-------------------------------

#log
-dontwarn com.nld.logger.**
-keep class com.nld.logger.**{*;}
#log4j
-dontwarn org.apache.log4j.**
-keep class  org.apache.log4j.** { *;}
#core-3.1.0(主要是二维码)
-keep class com.google.zxing.** {*;}
-dontwarn com.google.zxing.**
#cloudposApi-1.0.1(usdk)
-keep class com.example.cloudposapi.** {*;}
-keep class com.nld.cloudpos.** {*;}
#gson
-keep class com.google.gson.** {*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.** {
    <fields>;
    <methods>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-dontwarn com.google.gson.**
#support-v4
-keep class android.support.v4.** { *; }
-dontwarn android.support.v4.**
#bouncycastle
-keep class org.bouncycastle.**{ *; }
-dontwarn org.bouncycastle.**
#commons
-dontwarn org.apache.commons.codec.**
-keep class org.apache.commons.codec.**{*;}
#google
-dontwarn com.google.common.**
-keep class com.google.common.**{*;}
#fastjson
-keepattributes Signature,InnerClasses,*Annotation*
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
# xUtils
-keep class com.lidroid.xutils.** { *; }
-keep public class * extends com.lidroid.xutils.**
-keepattributes Signature
-keepattributes *Annotation*
-keep public interface com.lidroid.xutils.** {*;}
-dontwarn com.lidroid.xutils.**
-keepclasseswithmembers class com.jph.android.entity.** {
    <fields>;
    <methods>;
}
#pullRefresh
-dontwarn com.handmark.**
-keep class org.openudid.** { *; }
#-------------------------------------------------------------------------

#---------------------------------3.与js互相调用的类------------------------

-keepclasseswithmembers class com.demo.login.bean.ui.MainActivity$JSInterface {
      <methods>;
}

#-------------------------------------------------------------------------

#---------------------------------4.反射相关的类和方法-----------------------

#TODO 我的工程里没有。。。

#----------------------------------------------------------------------------

#-------------------------------------------基本不用动区域--------------------------------------------
#---------------------------------基本指令区----------------------------------
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
#----------------------------------------------------------------------------

#---------------------------------默认保留区---------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class **.R$* {
 *;
}
-keepclassmembers class * {
    void *(**On*Event);
}
#----------------------------------------------------------------------------
