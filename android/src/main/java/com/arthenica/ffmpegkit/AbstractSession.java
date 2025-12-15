package com.arthenica.ffmpegkit;

import com.arthenica.smartexception.java9.Exceptions;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractSession implements Session {
   protected static final AtomicLong sessionIdGenerator = new AtomicLong(1L);
   public static final int DEFAULT_TIMEOUT_FOR_ASYNCHRONOUS_MESSAGES_IN_TRANSMIT = 5000;
   protected final long sessionId;
   protected final LogCallback logCallback;
   protected final Date createTime;
   protected Date startTime;
   protected Date endTime;
   protected final String[] arguments;
   protected final List<Log> logs;
   protected final Object logsLock;
   protected Future<?> future;
   protected SessionState state;
   protected ReturnCode returnCode;
   protected String failStackTrace;
   protected final LogRedirectionStrategy logRedirectionStrategy;

   protected AbstractSession(String[] arguments, LogCallback logCallback, LogRedirectionStrategy logRedirectionStrategy) {
      this.sessionId = sessionIdGenerator.getAndIncrement();
      this.logCallback = logCallback;
      this.createTime = new Date();
      this.startTime = null;
      this.endTime = null;
      this.arguments = arguments;
      this.logs = new LinkedList();
      this.logsLock = new Object();
      this.future = null;
      this.state = SessionState.CREATED;
      this.returnCode = null;
      this.failStackTrace = null;
      this.logRedirectionStrategy = logRedirectionStrategy;
      FFmpegKitConfig.addSession(this);
   }

   public LogCallback getLogCallback() {
      return this.logCallback;
   }

   public long getSessionId() {
      return this.sessionId;
   }

   public Date getCreateTime() {
      return this.createTime;
   }

   public Date getStartTime() {
      return this.startTime;
   }

   public Date getEndTime() {
      return this.endTime;
   }

   public long getDuration() {
      Date startTime = this.startTime;
      Date endTime = this.endTime;
      return startTime != null && endTime != null ? endTime.getTime() - startTime.getTime() : 0L;
   }

   public String[] getArguments() {
      return this.arguments;
   }

   public String getCommand() {
      return FFmpegKitConfig.argumentsToString(this.arguments);
   }

   public List<Log> getAllLogs(int waitTimeout) {
      this.waitForAsynchronousMessagesInTransmit(waitTimeout);
      if (this.thereAreAsynchronousMessagesInTransmit()) {
         android.util.Log.i("ffmpeg-kit", String.format("getAllLogs was called to return all logs but there are still logs being transmitted for session id %d.", this.sessionId));
      }

      return this.getLogs();
   }

   public List<Log> getAllLogs() {
      return this.getAllLogs(5000);
   }

   public List<Log> getLogs() {
      synchronized(this.logsLock) {
         return new LinkedList(this.logs);
      }
   }

   public String getAllLogsAsString(int waitTimeout) {
      this.waitForAsynchronousMessagesInTransmit(waitTimeout);
      if (this.thereAreAsynchronousMessagesInTransmit()) {
         android.util.Log.i("ffmpeg-kit", String.format("getAllLogsAsString was called to return all logs but there are still logs being transmitted for session id %d.", this.sessionId));
      }

      return this.getLogsAsString();
   }

   public String getAllLogsAsString() {
      return this.getAllLogsAsString(5000);
   }

   public String getLogsAsString() {
      StringBuilder concatenatedString = new StringBuilder();
      synchronized(this.logsLock) {
         Iterator var3 = this.logs.iterator();

         while(var3.hasNext()) {
            Log log = (Log)var3.next();
            concatenatedString.append(log.getMessage());
         }

         return concatenatedString.toString();
      }
   }

   public String getOutput() {
      return this.getAllLogsAsString();
   }

   public SessionState getState() {
      return this.state;
   }

   public ReturnCode getReturnCode() {
      return this.returnCode;
   }

   public String getFailStackTrace() {
      return this.failStackTrace;
   }

   public LogRedirectionStrategy getLogRedirectionStrategy() {
      return this.logRedirectionStrategy;
   }

   public boolean thereAreAsynchronousMessagesInTransmit() {
      return FFmpegKitConfig.messagesInTransmit(this.sessionId) != 0;
   }

   public void addLog(Log log) {
      synchronized(this.logsLock) {
         this.logs.add(log);
      }
   }

   public Future<?> getFuture() {
      return this.future;
   }

   public void cancel() {
      if (this.state == SessionState.RUNNING) {
         FFmpegKit.cancel(this.sessionId);
      }

   }

   protected void waitForAsynchronousMessagesInTransmit(int timeout) {
      long start = System.currentTimeMillis();

      while(this.thereAreAsynchronousMessagesInTransmit() && System.currentTimeMillis() < start + (long)timeout) {
         synchronized(this) {
            try {
               this.wait(100L);
            } catch (InterruptedException var7) {
            }
         }
      }

   }

   void setFuture(Future<?> future) {
      this.future = future;
   }

   void startRunning() {
      this.state = SessionState.RUNNING;
      this.startTime = new Date();
   }

   void complete(ReturnCode returnCode) {
      this.returnCode = returnCode;
      this.state = SessionState.COMPLETED;
      this.endTime = new Date();
   }

   void fail(Exception exception) {
      this.failStackTrace = Exceptions.getStackTraceString(exception);
      this.state = SessionState.FAILED;
      this.endTime = new Date();
   }
}
