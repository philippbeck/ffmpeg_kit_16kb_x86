package com.arthenica.ffmpegkit;

import java.util.LinkedList;
import java.util.List;

public class FFmpegSession extends AbstractSession implements Session {
   private final StatisticsCallback statisticsCallback;
   private final FFmpegSessionCompleteCallback completeCallback;
   private final List<Statistics> statistics;
   private final Object statisticsLock;

   public static FFmpegSession create(String[] arguments) {
      return new FFmpegSession(arguments, (FFmpegSessionCompleteCallback)null, (LogCallback)null, (StatisticsCallback)null, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFmpegSession create(String[] arguments, FFmpegSessionCompleteCallback completeCallback) {
      return new FFmpegSession(arguments, completeCallback, (LogCallback)null, (StatisticsCallback)null, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFmpegSession create(String[] arguments, FFmpegSessionCompleteCallback completeCallback, LogCallback logCallback, StatisticsCallback statisticsCallback) {
      return new FFmpegSession(arguments, completeCallback, logCallback, statisticsCallback, FFmpegKitConfig.getLogRedirectionStrategy());
   }

   public static FFmpegSession create(String[] arguments, FFmpegSessionCompleteCallback completeCallback, LogCallback logCallback, StatisticsCallback statisticsCallback, LogRedirectionStrategy logRedirectionStrategy) {
      return new FFmpegSession(arguments, completeCallback, logCallback, statisticsCallback, logRedirectionStrategy);
   }

   private FFmpegSession(String[] arguments, FFmpegSessionCompleteCallback completeCallback, LogCallback logCallback, StatisticsCallback statisticsCallback, LogRedirectionStrategy logRedirectionStrategy) {
      super(arguments, logCallback, logRedirectionStrategy);
      this.completeCallback = completeCallback;
      this.statisticsCallback = statisticsCallback;
      this.statistics = new LinkedList();
      this.statisticsLock = new Object();
   }

   public StatisticsCallback getStatisticsCallback() {
      return this.statisticsCallback;
   }

   public FFmpegSessionCompleteCallback getCompleteCallback() {
      return this.completeCallback;
   }

   public List<Statistics> getAllStatistics(int waitTimeout) {
      this.waitForAsynchronousMessagesInTransmit(waitTimeout);
      if (this.thereAreAsynchronousMessagesInTransmit()) {
         android.util.Log.i("ffmpeg-kit", String.format("getAllStatistics was called to return all statistics but there are still statistics being transmitted for session id %d.", this.sessionId));
      }

      return this.getStatistics();
   }

   public List<Statistics> getAllStatistics() {
      return this.getAllStatistics(5000);
   }

   public List<Statistics> getStatistics() {
      synchronized(this.statisticsLock) {
         return this.statistics;
      }
   }

   public Statistics getLastReceivedStatistics() {
      synchronized(this.statisticsLock) {
         return this.statistics.size() > 0 ? (Statistics)this.statistics.get(this.statistics.size() - 1) : null;
      }
   }

   public void addStatistics(Statistics statistics) {
      synchronized(this.statisticsLock) {
         this.statistics.add(statistics);
      }
   }

   public boolean isFFmpeg() {
      return true;
   }

   public boolean isFFprobe() {
      return false;
   }

   public boolean isMediaInformation() {
      return false;
   }

   public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("FFmpegSession{");
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
