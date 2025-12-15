package com.arthenica.ffmpegkit;

public class AbiDetect {
   static final String ARM_V7A = "arm-v7a";
   static final String ARM_V7A_NEON = "arm-v7a-neon";
   private static boolean armV7aNeonLoaded = false;

   private AbiDetect() {
   }

   static void setArmV7aNeonLoaded() {
      armV7aNeonLoaded = true;
   }

   public static String getAbi() {
      return armV7aNeonLoaded ? "arm-v7a-neon" : getNativeAbi();
   }

   public static String getCpuAbi() {
      return getNativeCpuAbi();
   }

   static native String getNativeAbi();

   static native String getNativeCpuAbi();

   static native boolean isNativeLTSBuild();

   static native String getNativeBuildConf();

   static {
      NativeLoader.loadFFmpegKitAbiDetect();
      FFmpegKit.class.getName();
      FFmpegKitConfig.class.getName();
      FFprobeKit.class.getName();
   }
}
