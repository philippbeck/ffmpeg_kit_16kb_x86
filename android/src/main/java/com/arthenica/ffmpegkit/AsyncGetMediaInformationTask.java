package com.arthenica.ffmpegkit;

import com.arthenica.smartexception.java9.Exceptions;

public class AsyncGetMediaInformationTask implements Runnable {
   private final MediaInformationSession mediaInformationSession;
   private final MediaInformationSessionCompleteCallback completeCallback;
   private final Integer waitTimeout;

   public AsyncGetMediaInformationTask(MediaInformationSession mediaInformationSession) {
      this(mediaInformationSession, 5000);
   }

   public AsyncGetMediaInformationTask(MediaInformationSession mediaInformationSession, Integer waitTimeout) {
      this.mediaInformationSession = mediaInformationSession;
      this.completeCallback = mediaInformationSession.getCompleteCallback();
      this.waitTimeout = waitTimeout;
   }

   public void run() {
      FFmpegKitConfig.getMediaInformationExecute(this.mediaInformationSession, this.waitTimeout);
      if (this.completeCallback != null) {
         try {
            this.completeCallback.apply(this.mediaInformationSession);
         } catch (Exception var4) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside session complete callback.%s", Exceptions.getStackTraceString(var4)));
         }
      }

      MediaInformationSessionCompleteCallback globalMediaInformationSessionCompleteCallback = FFmpegKitConfig.getMediaInformationSessionCompleteCallback();
      if (globalMediaInformationSessionCompleteCallback != null) {
         try {
            globalMediaInformationSessionCompleteCallback.apply(this.mediaInformationSession);
         } catch (Exception var3) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside global complete callback.%s", Exceptions.getStackTraceString(var3)));
         }
      }

   }
}
