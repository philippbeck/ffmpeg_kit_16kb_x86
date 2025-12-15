package com.arthenica.ffmpegkit;


import com.arthenica.smartexception.java9.Exceptions;

public class AsyncFFmpegExecuteTask implements Runnable {
   private final FFmpegSession ffmpegSession;
   private final FFmpegSessionCompleteCallback completeCallback;

   public AsyncFFmpegExecuteTask(FFmpegSession ffmpegSession) {
      this.ffmpegSession = ffmpegSession;
      this.completeCallback = ffmpegSession.getCompleteCallback();
   }

   public void run() {
      FFmpegKitConfig.ffmpegExecute(this.ffmpegSession);
      if (this.completeCallback != null) {
         try {
            this.completeCallback.apply(this.ffmpegSession);
         } catch (Exception var4) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside session complete callback.%s", Exceptions.getStackTraceString(var4)));
         }
      }

      FFmpegSessionCompleteCallback globalFFmpegSessionCompleteCallback = FFmpegKitConfig.getFFmpegSessionCompleteCallback();
      if (globalFFmpegSessionCompleteCallback != null) {
         try {
            globalFFmpegSessionCompleteCallback.apply(this.ffmpegSession);
         } catch (Exception var3) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside global complete callback.%s", Exceptions.getStackTraceString(var3)));
         }
      }

   }
}
