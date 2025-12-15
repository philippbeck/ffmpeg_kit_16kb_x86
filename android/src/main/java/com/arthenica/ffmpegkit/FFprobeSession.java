package com.arthenica.ffmpegkit;

public class FFprobeSession extends AbstractSession implements Session {
   private final FFprobeSessionCompleteCallback completeCallback;

   public static FFprobeSession create(String[] arguments) {
      return new FFprobeSession(arguments, (FFprobeSessionCompleteCallback)null, (LogCallback)null, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFprobeSession create(String[] arguments, FFprobeSessionCompleteCallback completeCallback) {
      return new FFprobeSession(arguments, completeCallback, (LogCallback)null, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFprobeSession create(String[] arguments, FFprobeSessionCompleteCallback completeCallback, LogCallback logCallback) {
      return new FFprobeSession(arguments, completeCallback, logCallback, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFprobeSession create(String[] arguments, FFprobeSessionCompleteCallback completeCallback, LogCallback logCallback, LogRedirectionStrategy logRedirectionStrategy) {
      return new FFprobeSession(arguments, completeCallback, logCallback, logRedirectionStrategy);
   }

   private FFprobeSession(String[] arguments, FFprobeSessionCompleteCallback completeCallback, LogCallback logCallback, LogRedirectionStrategy logRedirectionStrategy) {
      super(arguments, logCallback, logRedirectionStrategy);
      this.completeCallback = completeCallback;
   }

   public FFprobeSessionCompleteCallback getCompleteCallback() {
      return this.completeCallback;
   }

   public boolean isFFmpeg() {
      return false;
   }

   public boolean isFFprobe() {
      return true;
   }

   public boolean isMediaInformation() {
      return false;
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("FFprobeSession{");
      stringBuilder.append("sessionId=");
      stringBuilder.append(this.sessionId);
      stringBuilder.append(", createTime=");
      stringBuilder.append(this.createTime);
      stringBuilder.append(", startTime=");
      stringBuilder.append(this.startTime);
      stringBuilder.append(", endTime=");
      stringBuilder.append(this.endTime);
      stringBuilder.append(", arguments=");
      stringBuilder.append(FFmpegKitConfig.argumentsToString(this.arguments));
      stringBuilder.append(", logs=");
      stringBuilder.append(this.getLogsAsString());
      stringBuilder.append(", state=");
      stringBuilder.append(this.state);
      stringBuilder.append(", returnCode=");
      stringBuilder.append(this.returnCode);
      stringBuilder.append(", failStackTrace=");
      stringBuilder.append('\'');
      stringBuilder.append(this.failStackTrace);
      stringBuilder.append('\'');
      stringBuilder.append('}');
      return stringBuilder.toString();
   }
}
