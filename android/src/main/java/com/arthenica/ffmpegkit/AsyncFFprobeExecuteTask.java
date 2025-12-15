package com.arthenica.ffmpegkit;

//import com.arthenica.smartexception.java9.Exceptions;

import com.arthenica.smartexception.java9.Exceptions;

public class AsyncFFprobeExecuteTask implements Runnable {
   private final FFprobeSession ffprobeSession;
   private final FFprobeSessionCompleteCallback completeCallback;

   public AsyncFFprobeExecuteTask(FFprobeSession ffprobeSession) {
      this.ffprobeSession = ffprobeSession;
      this.completeCallback = ffprobeSession.getCompleteCallback();
   }

   public void run() {
      FFmpegKitConfig.ffprobeExecute(this.ffprobeSession);
      if (this.completeCallback != null) {
         try {
            this.completeCallback.apply(this.ffprobeSession);
         } catch (Exception var4) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside session complete callback.%s", Exceptions.getStackTraceString(var4)));
         }
      }

      FFprobeSessionCompleteCallback globalFFprobeSessionCompleteCallback = FFmpegKitConfig.getFFprobeSessionCompleteCallback();
      if (globalFFprobeSessionCompleteCallback != null) {
         try {
            globalFFprobeSessionCompleteCallback.apply(this.ffprobeSession);
         } catch (Exception var3) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside global complete callback.%s", Exceptions.getStackTraceString(var3)));
         }
      }

   }
}
