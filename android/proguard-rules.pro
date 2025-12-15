# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.kts.
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontoptimize
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.android.** {*;}



# FFmpeg Kit 相关类保持不混淆
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.smartexception.** { *; }

# 保持 JNI 方法和原生库加载器
-keepclasseswithmembers class * {
    native <methods>;
}

# 保持用于原生库加载的类
-keep class com.arthenica.ffmpegkit.NativeLoader { *; }
-keep class com.arthenica.ffmpegkit.FFmpegKitConfig { *; }
-keep class com.arthenica.ffmpegkit.Abi { *; }
-keep class com.arthenica.ffmpegkit.AbiDetect { *; }

# 保持回调接口
-keep interface com.arthenica.ffmpegkit.*Callback { *; }

# 保持枚举类
-keepclassmembers enum com.arthenica.ffmpegkit.** { *; }

#-keep public class com.adjust.sdk.**{ *; }
#-keep class com.google.android.gms.common.ConnectionResult {
#    int SUCCESS;
#}
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
#    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
#}
#-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
#    java.lang.String getId();
#    boolean isLimitAdTrackingEnabled();
#}
#-keep public class com.android.installreferrer.**{ *; }

