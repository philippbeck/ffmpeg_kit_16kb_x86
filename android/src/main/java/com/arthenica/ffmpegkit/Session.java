package com.arthenica.ffmpegkit;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

public interface Session {
   LogCallback getLogCallback();

   long getSessionId();

   Date getCreateTime();

   Date getStartTime();

   Date getEndTime();

   long getDuration();

   String[] getArguments();

   String getCommand();

   List<Log> getAllLogs(int var1);

   List<Log> getAllLogs();

   List<Log> getLogs();

   String getAllLogsAsString(int var1);

   String getAllLogsAsString();

   String getLogsAsString();

   String getOutput();

   SessionState getState();

   ReturnCode getReturnCode();

   String getFailStackTrace();

   LogRedirectionStrategy getLogRedirectionStrategy();

   boolean thereAreAsynchronousMessagesInTransmit();

   void addLog(Log var1);

   Future<?> getFuture();

   boolean isFFmpeg();

   boolean isFFprobe();

   boolean isMediaInformation();

   void cancel();
}
