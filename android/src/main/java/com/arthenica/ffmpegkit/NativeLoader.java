package com.arthenica.ffmpegkit;

import android.os.Build;
import android.os.Build.VERSION;

import com.arthenica.smartexception.java9.Exceptions;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NativeLoader {
   static final String[] FFMPEG_LIBRARIES = new String[]{"avutil", "swscale", "swresample", "avcodec", "avformat", "avfilter", "avdevice"};
   static final String[] LIBRARIES_LINKED_WITH_CXX = new String[]{"chromaprint", "openh264", "rubberband", "snappy", "srt", "tesseract", "x265", "zimg", "libilbc"};

   static boolean isTestModeDisabled() {
      return System.getProperty("enable.ffmpeg.kit.test.mode") == null;
   }

   private static void loadLibrary(String libraryName) {
      if (isTestModeDisabled()) {
         try {
            System.loadLibrary(libraryName);
         } catch (UnsatisfiedLinkError var2) {
            throw new Error(String.format("FFmpegKit failed to start on %s.", getDeviceDebugInformation()), var2);
         }
      }

   }

   private static List<String> loadExternalLibraries() {
      return isTestModeDisabled() ? Packages.getExternalLibraries() : Collections.emptyList();
   }

   private static String loadNativeAbi() {
      return isTestModeDisabled() ? AbiDetect.getNativeAbi() : Abi.ABI_X86_64.getName();
   }

   static String loadAbi() {
      return isTestModeDisabled() ? AbiDetect.getAbi() : Abi.ABI_X86_64.getName();
   }

   static String loadPackageName() {
      return isTestModeDisabled() ? Packages.getPackageName() : "test";
   }

   static String loadVersion() {
      String version = "6.0";
      if (isTestModeDisabled()) {
         return FFmpegKitConfig.getVersion();
      } else {
         return loadIsLTSBuild() ? String.format("%s-lts", "6.0") : "6.0";
      }
   }

   static boolean loadIsLTSBuild() {
      return isTestModeDisabled() ? AbiDetect.isNativeLTSBuild() : true;
   }

   static int loadLogLevel() {
      return isTestModeDisabled() ? FFmpegKitConfig.getNativeLogLevel() : Level.AV_LOG_DEBUG.getValue();
   }

   static String loadBuildDate() {
      return isTestModeDisabled() ? FFmpegKitConfig.getBuildDate() : (new SimpleDateFormat("yyyyMMdd", Locale.getDefault())).format(new Date());
   }

   static void enableRedirection() {
      if (isTestModeDisabled()) {
         FFmpegKitConfig.enableRedirection();
      }

   }

   static void loadFFmpegKitAbiDetect() {
      loadLibrary("ffmpegkit_abidetect");
   }

   static boolean loadFFmpeg() {
      boolean nativeFFmpegLoaded = false;
      boolean nativeFFmpegTriedAndFailed = false;
      if (VERSION.SDK_INT < 21) {
         List<String> externalLibrariesEnabled = loadExternalLibraries();
         String[] var3 = LIBRARIES_LINKED_WITH_CXX;
         int var4 = var3.length;

         int var5;
         String ffmpegLibrary;
         for(var5 = 0; var5 < var4; ++var5) {
            ffmpegLibrary = var3[var5];
            if (externalLibrariesEnabled.contains(ffmpegLibrary)) {
               loadLibrary("c++_shared");
               break;
            }
         }

         if ("arm-v7a".equals(loadNativeAbi())) {
            try {
               var3 = FFMPEG_LIBRARIES;
               var4 = var3.length;

               for(var5 = 0; var5 < var4; ++var5) {
                  ffmpegLibrary = var3[var5];
                  loadLibrary(ffmpegLibrary + "_neon");
               }

               nativeFFmpegLoaded = true;
            } catch (Error var7) {
               android.util.Log.i("ffmpeg-kit", String.format("NEON supported armeabi-v7a ffmpeg library not found. Loading default armeabi-v7a library.%s", Exceptions.getStackTraceString(var7)));
               nativeFFmpegTriedAndFailed = true;
            }
         }

         if (!nativeFFmpegLoaded) {
            var3 = FFMPEG_LIBRARIES;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               ffmpegLibrary = var3[var5];
               loadLibrary(ffmpegLibrary);
            }
         }
      }

      return nativeFFmpegTriedAndFailed;
   }

   static void loadFFmpegKit(boolean nativeFFmpegTriedAndFailed) {
      boolean nativeFFmpegKitLoaded = false;
      if (!nativeFFmpegTriedAndFailed && "arm-v7a".equals(loadNativeAbi())) {
         try {
            loadLibrary("ffmpegkit_armv7a_neon");
            nativeFFmpegKitLoaded = true;
            AbiDetect.setArmV7aNeonLoaded();
         } catch (Error var3) {
            android.util.Log.i("ffmpeg-kit", String.format("NEON supported armeabi-v7a ffmpegkit library not found. Loading default armeabi-v7a library.%s", Exceptions.getStackTraceString(var3)));
         }
      }

      if (!nativeFFmpegKitLoaded) {
         loadLibrary("ffmpegkit");
      }

   }

   static String getDeviceDebugInformation() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("brand: ");
      stringBuilder.append(Build.BRAND);
      stringBuilder.append(", model: ");
      stringBuilder.append(Build.MODEL);
      stringBuilder.append(", device: ");
      stringBuilder.append(Build.DEVICE);
      stringBuilder.append(", api level: ");
      stringBuilder.append(VERSION.SDK_INT);
//      if (VERSION.SDK_INT >= 21) {
//         stringBuilder.append(", abis: ");
//         stringBuilder.append(FFmpegKitConfig.argumentsToString(Build.SUPPORTED_ABIS));
//         stringBuilder.append(", 32bit abis: ");
//         stringBuilder.append(FFmpegKitConfig.argumentsToString(Build.SUPPORTED_32_BIT_ABIS));
//         stringBuilder.append(", 64bit abis: ");
//         stringBuilder.append(FFmpegKitConfig.argumentsToString(Build.SUPPORTED_64_BIT_ABIS));
//      } else {
         String[] supportedAbis = Build.SUPPORTED_ABIS;

//         stringBuilder.append(", cpu abis: ");
//         stringBuilder.append(Build.CPU_ABI);
//         stringBuilder.append(", cpu abi2s: ");
//         stringBuilder.append(Build.CPU_ABI2);
         stringBuilder.append(String.join(", ", supportedAbis));
//      }

      return stringBuilder.toString();
   }
}
