package com.arthenica.ffmpegkit;

public class MediaInformationSession extends AbstractSession implements Session {
   private MediaInformation mediaInformation;
   private final MediaInformationSessionCompleteCallback completeCallback;

   public static MediaInformationSession create(String[] arguments) {
      return new MediaInformationSession(arguments, (MediaInformationSessionCompleteCallback)null, (LogCallback)null);
   }

   public static MediaInformationSession create(String[] arguments, MediaInformationSessionCompleteCallback completeCallback) {
      return new MediaInformationSession(arguments, completeCallback, (LogCallback)null);
   }

   public static MediaInformationSession create(String[] arguments, MediaInformationSessionCompleteCallback completeCallback, LogCallback logCallback) {
      return new MediaInformationSession(arguments, completeCallback, logCallback);
   }

   private MediaInformationSession(String[] arguments, MediaInformationSessionCompleteCallback completeCallback, LogCallback logCallback) {
      super(arguments, logCallback, LogRedirectionStrategy.NEVER_PRINT_LOGS);
      this.completeCallback = completeCallback;
   }

   public MediaInformation getMediaInformation() {
      return this.mediaInformation;
   }

   public void setMediaInformation(MediaInformation mediaInformation) {
      this.mediaInformation = mediaInformation;
   }

   public MediaInformationSessionCompleteCallback getCompleteCallback() {
      return this.completeCallback;
   }

   public boolean isFFmpeg() {
      return false;
   }

   public boolean isFFprobe() {
      return false;
   }

   public boolean isMediaInformation() {
      return true;
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("MediaInformationSession{");
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
