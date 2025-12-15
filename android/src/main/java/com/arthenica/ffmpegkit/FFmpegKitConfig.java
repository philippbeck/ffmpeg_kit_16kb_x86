package com.arthenica.ffmpegkit;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;

import com.arthenica.smartexception.java9.Exceptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FFmpegKitConfig {
   static final String TAG = "ffmpeg-kit";
   static final String FFMPEG_KIT_NAMED_PIPE_PREFIX = "fk_pipe_";
   private static final AtomicInteger uniqueIdGenerator;
   private static Level activeLogLevel;
   private static int sessionHistorySize;
   private static final Map<Long, Session> sessionHistoryMap;
   private static final List<Session> sessionHistoryList;
   private static final Object sessionHistoryLock;
   private static int asyncConcurrencyLimit;
   private static ExecutorService asyncExecutorService;
   private static LogCallback globalLogCallback;
   private static StatisticsCallback globalStatisticsCallback;
   private static FFmpegSessionCompleteCallback globalFFmpegSessionCompleteCallback;
   private static FFprobeSessionCompleteCallback globalFFprobeSessionCompleteCallback;
   private static MediaInformationSessionCompleteCallback globalMediaInformationSessionCompleteCallback;
   private static final SparseArray<SAFProtocolUrl> safIdMap;
   private static final SparseArray<SAFProtocolUrl> safFileDescriptorMap;
   private static LogRedirectionStrategy globalLogRedirectionStrategy;

   private FFmpegKitConfig() {
   }

   public static void enableRedirection() {
      enableNativeRedirection();
   }

   public static void disableRedirection() {
      disableNativeRedirection();
   }

   private static void log(long sessionId, int levelValue, byte[] logMessage) {
      Level level = Level.from(levelValue);
      String text = new String(logMessage);
      Log log = new Log(sessionId, level, text);
      boolean globalCallbackDefined = false;
      boolean sessionCallbackDefined = false;
      LogRedirectionStrategy activeLogRedirectionStrategy = globalLogRedirectionStrategy;
      if ((activeLogLevel != Level.AV_LOG_QUIET || levelValue == Level.AV_LOG_STDERR.getValue()) && levelValue <= activeLogLevel.getValue()) {
         Session session = getSession(sessionId);
         if (session != null) {
            activeLogRedirectionStrategy = session.getLogRedirectionStrategy();
            session.addLog(log);
            if (session.getLogCallback() != null) {
               sessionCallbackDefined = true;

               try {
                  session.getLogCallback().apply(log);
               } catch (Exception var14) {
                  android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside session log callback.%s", Exceptions.getStackTraceString(var14)));
               }
            }
         }

         LogCallback globalLogCallbackFunction = globalLogCallback;
         if (globalLogCallbackFunction != null) {
            globalCallbackDefined = true;

            try {
               globalLogCallbackFunction.apply(log);
            } catch (Exception var13) {
               android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside global log callback.%s", Exceptions.getStackTraceString(var13)));
            }
         }

         switch(activeLogRedirectionStrategy) {
         case NEVER_PRINT_LOGS:
            return;
         case PRINT_LOGS_WHEN_GLOBAL_CALLBACK_NOT_DEFINED:
            if (globalCallbackDefined) {
               return;
            }
            break;
         case PRINT_LOGS_WHEN_SESSION_CALLBACK_NOT_DEFINED:
            if (sessionCallbackDefined) {
               return;
            }
            break;
         case PRINT_LOGS_WHEN_NO_CALLBACKS_DEFINED:
            if (globalCallbackDefined || sessionCallbackDefined) {
               return;
            }
         case ALWAYS_PRINT_LOGS:
         }

         switch(level) {
         case AV_LOG_QUIET:
            break;
         case AV_LOG_TRACE:
         case AV_LOG_DEBUG:
            android.util.Log.d("ffmpeg-kit", text);
            break;
         case AV_LOG_INFO:
            android.util.Log.i("ffmpeg-kit", text);
            break;
         case AV_LOG_WARNING:
            android.util.Log.w("ffmpeg-kit", text);
            break;
         case AV_LOG_ERROR:
         case AV_LOG_FATAL:
         case AV_LOG_PANIC:
            android.util.Log.e("ffmpeg-kit", text);
            break;
         case AV_LOG_STDERR:
         case AV_LOG_VERBOSE:
         default:
            android.util.Log.v("ffmpeg-kit", text);
         }

      }
   }

   private static void statistics(long sessionId, int videoFrameNumber, float videoFps, float videoQuality, long size, double time, double bitrate, double speed) {
      Statistics statistics = new Statistics(sessionId, videoFrameNumber, videoFps, videoQuality, size, time, bitrate, speed);
      Session session = getSession(sessionId);
      if (session != null && session.isFFmpeg()) {
         FFmpegSession ffmpegSession = (FFmpegSession)session;
         ffmpegSession.addStatistics(statistics);
         if (ffmpegSession.getStatisticsCallback() != null) {
            try {
               ffmpegSession.getStatisticsCallback().apply(statistics);
            } catch (Exception var18) {
               android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside session statistics callback.%s", Exceptions.getStackTraceString(var18)));
            }
         }
      }

      StatisticsCallback globalStatisticsCallbackFunction = globalStatisticsCallback;
      if (globalStatisticsCallbackFunction != null) {
         try {
            globalStatisticsCallbackFunction.apply(statistics);
         } catch (Exception var17) {
            android.util.Log.e("ffmpeg-kit", String.format("Exception thrown inside global statistics callback.%s", Exceptions.getStackTraceString(var17)));
         }
      }

   }

   public static int setFontconfigConfigurationPath(String path) {
      return setNativeEnvironmentVariable("FONTCONFIG_PATH", path);
   }

   public static void setFontDirectory(Context context, String fontDirectoryPath, Map<String, String> fontNameMapping) {
      setFontDirectoryList(context, Collections.singletonList(fontDirectoryPath), fontNameMapping);
   }

   public static void setFontDirectoryList(Context context, List<String> fontDirectoryList, Map<String, String> fontNameMapping) {
      File cacheDir = context.getCacheDir();
      int validFontNameMappingCount = 0;
      File tempConfigurationDirectory = new File(cacheDir, "fontconfig");
      if (!tempConfigurationDirectory.exists()) {
         boolean tempFontConfDirectoryCreated = tempConfigurationDirectory.mkdirs();
         android.util.Log.d("ffmpeg-kit", String.format("Created temporary font conf directory: %s.", tempFontConfDirectoryCreated));
      }

      File fontConfiguration = new File(tempConfigurationDirectory, "fonts.conf");
      if (fontConfiguration.exists()) {
         boolean fontConfigurationDeleted = fontConfiguration.delete();
         android.util.Log.d("ffmpeg-kit", String.format("Deleted old temporary font configuration: %s.", fontConfigurationDeleted));
      }

      StringBuilder fontNameMappingBlock = new StringBuilder("");
      String fontName;
      if (fontNameMapping != null && fontNameMapping.size() > 0) {
         fontNameMapping.entrySet();
         Iterator var8 = fontNameMapping.entrySet().iterator();

         while(var8.hasNext()) {
            Entry<String, String> mapping = (Entry)var8.next();
            fontName = (String)mapping.getKey();
            String mappedFontName = (String)mapping.getValue();
            if (fontName != null && mappedFontName != null && fontName.trim().length() > 0 && mappedFontName.trim().length() > 0) {
               fontNameMappingBlock.append("    <match target=\"pattern\">\n");
               fontNameMappingBlock.append("        <test qual=\"any\" name=\"family\">\n");
               fontNameMappingBlock.append(String.format("            <string>%s</string>\n", fontName));
               fontNameMappingBlock.append("        </test>\n");
               fontNameMappingBlock.append("        <edit name=\"family\" mode=\"assign\" binding=\"same\">\n");
               fontNameMappingBlock.append(String.format("            <string>%s</string>\n", mappedFontName));
               fontNameMappingBlock.append("        </edit>\n");
               fontNameMappingBlock.append("    </match>\n");
               ++validFontNameMappingCount;
            }
         }
      }

      StringBuilder fontConfigBuilder = new StringBuilder();
      fontConfigBuilder.append("<?xml version=\"1.0\"?>\n");
      fontConfigBuilder.append("<!DOCTYPE fontconfig SYSTEM \"fonts.dtd\">\n");
      fontConfigBuilder.append("<fontconfig>\n");
      fontConfigBuilder.append("    <dir prefix=\"cwd\">.</dir>\n");
      Iterator var26 = fontDirectoryList.iterator();

      while(var26.hasNext()) {
         fontName = (String)var26.next();
         fontConfigBuilder.append("    <dir>");
         fontConfigBuilder.append(fontName);
         fontConfigBuilder.append("</dir>\n");
      }

      fontConfigBuilder.append(fontNameMappingBlock);
      fontConfigBuilder.append("</fontconfig>\n");
      AtomicReference reference = new AtomicReference();

      try {
         FileOutputStream outputStream = new FileOutputStream(fontConfiguration);
         reference.set(outputStream);
         outputStream.write(fontConfigBuilder.toString().getBytes());
         outputStream.flush();
         android.util.Log.d("ffmpeg-kit", String.format("Saved new temporary font configuration with %d font name mappings.", validFontNameMappingCount));
         setFontconfigConfigurationPath(tempConfigurationDirectory.getAbsolutePath());
         Iterator var29 = fontDirectoryList.iterator();

         while(var29.hasNext()) {
            String fontDirectoryPath = (String)var29.next();
            android.util.Log.d("ffmpeg-kit", String.format("Font directory %s registered successfully.", fontDirectoryPath));
         }
      } catch (IOException var21) {
         android.util.Log.e("ffmpeg-kit", String.format("Failed to set font directory: %s.%s", Arrays.toString(fontDirectoryList.toArray()), Exceptions.getStackTraceString(var21)));
      } finally {
         if (reference.get() != null) {
            try {
               ((FileOutputStream)reference.get()).close();
            } catch (IOException var20) {
            }
         }

      }

   }

   public static String registerNewFFmpegPipe(Context context) {
      File cacheDir = context.getCacheDir();
      File pipesDir = new File(cacheDir, "pipes");
      if (!pipesDir.exists()) {
         boolean pipesDirCreated = pipesDir.mkdirs();
         if (!pipesDirCreated) {
            android.util.Log.e("ffmpeg-kit", String.format("Failed to create pipes directory: %s.", pipesDir.getAbsolutePath()));
            return null;
         }
      }

      String newFFmpegPipePath = MessageFormat.format("{0}{1}{2}{3}", pipesDir, File.separator, "fk_pipe_", uniqueIdGenerator.getAndIncrement());
      closeFFmpegPipe(newFFmpegPipePath);
      int rc = registerNewNativeFFmpegPipe(newFFmpegPipePath);
      if (rc == 0) {
         return newFFmpegPipePath;
      } else {
         android.util.Log.e("ffmpeg-kit", String.format("Failed to register new FFmpeg pipe %s. Operation failed with rc=%d.", newFFmpegPipePath, rc));
         return null;
      }
   }

   public static void closeFFmpegPipe(String ffmpegPipePath) {
      File file = new File(ffmpegPipePath);
      if (file.exists()) {
         file.delete();
      }

   }

   public static List<String> getSupportedCameraIds(Context context) {
      List<String> detectedCameraIdList = new ArrayList();
      if (VERSION.SDK_INT >= 24) {
         detectedCameraIdList.addAll(CameraSupport.extractSupportedCameraIds(context));
      }

      return detectedCameraIdList;
   }

   public static String getFFmpegVersion() {
      return getNativeFFmpegVersion();
   }

   public static String getVersion() {
      return isLTSBuild() ? String.format("%s-lts", getNativeVersion()) : getNativeVersion();
   }

   public static boolean isLTSBuild() {
      return AbiDetect.isNativeLTSBuild();
   }

   public static String getBuildDate() {
      return getNativeBuildDate();
   }

   public static void printToLogcat(int logPriority, String string) {

      String remainingString = string;

      do {
         if (remainingString.length() <= 4000) {
            android.util.Log.println(logPriority, "ffmpeg-kit", remainingString);
            remainingString = "";
         } else {
            int index = remainingString.substring(0, 4000).lastIndexOf(10);
            if (index < 0) {
               android.util.Log.println(logPriority, "ffmpeg-kit", remainingString.substring(0, 4000));
               remainingString = remainingString.substring(4000);
            } else {
               android.util.Log.println(logPriority, "ffmpeg-kit", remainingString.substring(0, index));
               remainingString = remainingString.substring(index);
            }
         }
      } while(remainingString.length() > 0);

   }

   public static int setEnvironmentVariable(String variableName, String variableValue) {
      return setNativeEnvironmentVariable(variableName, variableValue);
   }

   public static void ignoreSignal(Signal signal) {
      ignoreNativeSignal(signal.getValue());
   }

   public static void ffmpegExecute(FFmpegSession ffmpegSession) {
      ffmpegSession.startRunning();

      try {
         int returnCode = nativeFFmpegExecute(ffmpegSession.getSessionId(), ffmpegSession.getArguments());
         ffmpegSession.complete(new ReturnCode(returnCode));
      } catch (Exception var2) {
         ffmpegSession.fail(var2);
         android.util.Log.w("ffmpeg-kit", String.format("FFmpeg execute failed: %s.%s", argumentsToString(ffmpegSession.getArguments()), Exceptions.getStackTraceString(var2)));
      }

   }

   public static void ffprobeExecute(FFprobeSession ffprobeSession) {
      ffprobeSession.startRunning();

      try {
         int returnCode = nativeFFprobeExecute(ffprobeSession.getSessionId(), ffprobeSession.getArguments());
         ffprobeSession.complete(new ReturnCode(returnCode));
      } catch (Exception var2) {
         ffprobeSession.fail(var2);
         android.util.Log.w("ffmpeg-kit", String.format("FFprobe execute failed: %s.%s", argumentsToString(ffprobeSession.getArguments()), Exceptions.getStackTraceString(var2)));
      }

   }

   public static void getMediaInformationExecute(MediaInformationSession mediaInformationSession, int waitTimeout) {
      mediaInformationSession.startRunning();

      try {
         int returnCodeValue = nativeFFprobeExecute(mediaInformationSession.getSessionId(), mediaInformationSession.getArguments());
         ReturnCode returnCode = new ReturnCode(returnCodeValue);
         mediaInformationSession.complete(returnCode);
         if (returnCode.isValueSuccess()) {
            List<Log> allLogs = mediaInformationSession.getAllLogs(waitTimeout);
            StringBuilder ffprobeJsonOutput = new StringBuilder();
            int i = 0;

            for(int allLogsSize = allLogs.size(); i < allLogsSize; ++i) {
               Log log = (Log)allLogs.get(i);
               if (log.getLevel() == Level.AV_LOG_STDERR) {
                  ffprobeJsonOutput.append(log.getMessage());
               }
            }

            MediaInformation mediaInformation = MediaInformationJsonParser.fromWithError(ffprobeJsonOutput.toString());
            mediaInformationSession.setMediaInformation(mediaInformation);
         }
      } catch (Exception var9) {
         mediaInformationSession.fail(var9);
         android.util.Log.w("ffmpeg-kit", String.format("Get media information execute failed: %s.%s", argumentsToString(mediaInformationSession.getArguments()), Exceptions.getStackTraceString(var9)));
      }

   }

   public static void asyncFFmpegExecute(FFmpegSession ffmpegSession) {
      AsyncFFmpegExecuteTask asyncFFmpegExecuteTask = new AsyncFFmpegExecuteTask(ffmpegSession);
      Future<?> future = asyncExecutorService.submit(asyncFFmpegExecuteTask);
      ffmpegSession.setFuture(future);
   }

   public static void asyncFFmpegExecute(FFmpegSession ffmpegSession, ExecutorService executorService) {
      AsyncFFmpegExecuteTask asyncFFmpegExecuteTask = new AsyncFFmpegExecuteTask(ffmpegSession);
      Future<?> future = executorService.submit(asyncFFmpegExecuteTask);
      ffmpegSession.setFuture(future);
   }

   public static void asyncFFprobeExecute(FFprobeSession ffprobeSession) {
      AsyncFFprobeExecuteTask asyncFFmpegExecuteTask = new AsyncFFprobeExecuteTask(ffprobeSession);
      Future<?> future = asyncExecutorService.submit(asyncFFmpegExecuteTask);
      ffprobeSession.setFuture(future);
   }

   public static void asyncFFprobeExecute(FFprobeSession ffprobeSession, ExecutorService executorService) {
      AsyncFFprobeExecuteTask asyncFFmpegExecuteTask = new AsyncFFprobeExecuteTask(ffprobeSession);
      Future<?> future = executorService.submit(asyncFFmpegExecuteTask);
      ffprobeSession.setFuture(future);
   }

   public static void asyncGetMediaInformationExecute(MediaInformationSession mediaInformationSession, int waitTimeout) {
      AsyncGetMediaInformationTask asyncGetMediaInformationTask = new AsyncGetMediaInformationTask(mediaInformationSession, waitTimeout);
      Future<?> future = asyncExecutorService.submit(asyncGetMediaInformationTask);
      mediaInformationSession.setFuture(future);
   }

   public static void asyncGetMediaInformationExecute(MediaInformationSession mediaInformationSession, ExecutorService executorService, int waitTimeout) {
      AsyncGetMediaInformationTask asyncGetMediaInformationTask = new AsyncGetMediaInformationTask(mediaInformationSession, waitTimeout);
      Future<?> future = executorService.submit(asyncGetMediaInformationTask);
      mediaInformationSession.setFuture(future);
   }

   public static int getAsyncConcurrencyLimit() {
      return asyncConcurrencyLimit;
   }

   public static void setAsyncConcurrencyLimit(int asyncConcurrencyLimit) {
      if (asyncConcurrencyLimit > 0) {
         FFmpegKitConfig.asyncConcurrencyLimit = asyncConcurrencyLimit;
         ExecutorService oldAsyncExecutorService = asyncExecutorService;
         asyncExecutorService = Executors.newFixedThreadPool(asyncConcurrencyLimit);
         oldAsyncExecutorService.shutdown();
      }

   }

   public static void enableLogCallback(LogCallback logCallback) {
      globalLogCallback = logCallback;
   }

   public static void enableStatisticsCallback(StatisticsCallback statisticsCallback) {
      globalStatisticsCallback = statisticsCallback;
   }

   public static void enableFFmpegSessionCompleteCallback(FFmpegSessionCompleteCallback ffmpegSessionCompleteCallback) {
      globalFFmpegSessionCompleteCallback = ffmpegSessionCompleteCallback;
   }

   public static FFmpegSessionCompleteCallback getFFmpegSessionCompleteCallback() {
      return globalFFmpegSessionCompleteCallback;
   }

   public static void enableFFprobeSessionCompleteCallback(FFprobeSessionCompleteCallback ffprobeSessionCompleteCallback) {
      globalFFprobeSessionCompleteCallback = ffprobeSessionCompleteCallback;
   }

   public static FFprobeSessionCompleteCallback getFFprobeSessionCompleteCallback() {
      return globalFFprobeSessionCompleteCallback;
   }

   public static void enableMediaInformationSessionCompleteCallback(MediaInformationSessionCompleteCallback mediaInformationSessionCompleteCallback) {
      globalMediaInformationSessionCompleteCallback = mediaInformationSessionCompleteCallback;
   }

   public static MediaInformationSessionCompleteCallback getMediaInformationSessionCompleteCallback() {
      return globalMediaInformationSessionCompleteCallback;
   }

   public static Level getLogLevel() {
      return activeLogLevel;
   }

   public static void setLogLevel(Level level) {
      if (level != null) {
         activeLogLevel = level;
         setNativeLogLevel(level.getValue());
      }

   }

   static String extractExtensionFromSafDisplayName(String safDisplayName) {
      String rawExtension = safDisplayName;
      if (safDisplayName.lastIndexOf(".") >= 0) {
         rawExtension = safDisplayName.substring(safDisplayName.lastIndexOf("."));
      }

      try {
         return (new StringTokenizer(rawExtension, " .")).nextToken();
      } catch (Exception var3) {
         android.util.Log.w("ffmpeg-kit", String.format("Failed to extract extension from saf display name: %s.%s", safDisplayName, Exceptions.getStackTraceString(var3)));
         return "raw";
      }
   }

   public static String getSafParameter(Context context, Uri uri, String openMode) {
      if (VERSION.SDK_INT < 19) {
         android.util.Log.i("ffmpeg-kit", String.format("getSafParameter is not supported on API Level %d", VERSION.SDK_INT));
         return "";
      } else {
         String displayName = "unknown";

         try {
            Cursor cursor = context.getContentResolver().query(uri, (String[])null, (String)null, (String[])null, (String)null);

            try {
               if (cursor != null && cursor.moveToFirst()) {
                  displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
               }
            } catch (Throwable var8) {
               if (cursor != null) {
                  try {
                     cursor.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (cursor != null) {
               cursor.close();
            }
         } catch (Throwable var9) {
            android.util.Log.e("ffmpeg-kit", String.format("Failed to get %s column for %s.%s", "_display_name", uri.toString(), Exceptions.getStackTraceString(var9)));
            throw var9;
         }

         int safId = uniqueIdGenerator.getAndIncrement();
         safIdMap.put(safId, new SAFProtocolUrl(safId, uri, openMode, context.getContentResolver()));
         return "saf:" + safId + "." + extractExtensionFromSafDisplayName(displayName);
      }
   }

   public static String getSafParameterForRead(Context context, Uri uri) {
      return getSafParameter(context, uri, "r");
   }

   public static String getSafParameterForWrite(Context context, Uri uri) {
      return getSafParameter(context, uri, "w");
   }

   private static int safOpen(int safId) {
      try {
         SAFProtocolUrl safUrl = (SAFProtocolUrl)safIdMap.get(safId);
         if (safUrl != null) {
            ParcelFileDescriptor parcelFileDescriptor = safUrl.getContentResolver().openFileDescriptor(safUrl.getUri(), safUrl.getOpenMode());
            safUrl.setParcelFileDescriptor(parcelFileDescriptor);
            int fd = parcelFileDescriptor.getFd();
            safFileDescriptorMap.put(fd, safUrl);
            return fd;
         }

         android.util.Log.e("ffmpeg-kit", String.format("SAF id %d not found.", safId));
      } catch (Throwable var4) {
         android.util.Log.e("ffmpeg-kit", String.format("Failed to open SAF id: %d.%s", safId, Exceptions.getStackTraceString(var4)));
      }

      return 0;
   }

   private static int safClose(int fileDescriptor) {
      try {
         SAFProtocolUrl safProtocolUrl = (SAFProtocolUrl)safFileDescriptorMap.get(fileDescriptor);
         if (safProtocolUrl != null) {
            ParcelFileDescriptor parcelFileDescriptor = safProtocolUrl.getParcelFileDescriptor();
            if (parcelFileDescriptor != null) {
               safFileDescriptorMap.delete(fileDescriptor);
               safIdMap.delete(safProtocolUrl.getSafId());
               parcelFileDescriptor.close();
               return 1;
            }

            android.util.Log.e("ffmpeg-kit", String.format("ParcelFileDescriptor for SAF fd %d not found.", fileDescriptor));
         } else {
            android.util.Log.e("ffmpeg-kit", String.format("SAF fd %d not found.", fileDescriptor));
         }
      } catch (Throwable var3) {
         android.util.Log.e("ffmpeg-kit", String.format("Failed to close SAF fd: %d.%s", fileDescriptor, Exceptions.getStackTraceString(var3)));
      }

      return 0;
   }

   public static int getSessionHistorySize() {
      return sessionHistorySize;
   }

   public static void setSessionHistorySize(int sessionHistorySize) {
      if (sessionHistorySize >= 1000) {
         throw new IllegalArgumentException("Session history size must not exceed the hard limit!");
      } else {
         if (sessionHistorySize > 0) {
            FFmpegKitConfig.sessionHistorySize = sessionHistorySize;
            deleteExpiredSessions();
         }

      }
   }

   private static void deleteExpiredSessions() {
      while(sessionHistoryList.size() > sessionHistorySize) {
         try {
            Session expiredSession = (Session)sessionHistoryList.remove(0);
            if (expiredSession != null) {
               sessionHistoryMap.remove(expiredSession.getSessionId());
            }
         } catch (IndexOutOfBoundsException var1) {
         }
      }

   }

   static void addSession(Session session) {
      synchronized(sessionHistoryLock) {
         boolean sessionAlreadyAdded = sessionHistoryMap.containsKey(session.getSessionId());
         if (!sessionAlreadyAdded) {
            sessionHistoryMap.put(session.getSessionId(), session);
            sessionHistoryList.add(session);
            deleteExpiredSessions();
         }

      }
   }

   public static Session getSession(long sessionId) {
      synchronized(sessionHistoryLock) {
         return (Session)sessionHistoryMap.get(sessionId);
      }
   }

   public static Session getLastSession() {
      synchronized(sessionHistoryLock) {
         return sessionHistoryList.size() > 0 ? (Session)sessionHistoryList.get(sessionHistoryList.size() - 1) : null;
      }
   }

   public static Session getLastCompletedSession() {
      synchronized(sessionHistoryLock) {
         for(int i = sessionHistoryList.size() - 1; i >= 0; --i) {
            Session session = (Session)sessionHistoryList.get(i);
            if (session.getState() == SessionState.COMPLETED) {
               return session;
            }
         }

         return null;
      }
   }

   public static List<Session> getSessions() {
      synchronized(sessionHistoryLock) {
         return new LinkedList(sessionHistoryList);
      }
   }

   public static void clearSessions() {
      synchronized(sessionHistoryLock) {
         sessionHistoryList.clear();
         sessionHistoryMap.clear();
      }
   }

   public static List<FFmpegSession> getFFmpegSessions() {
      LinkedList<FFmpegSession> list = new LinkedList();
      synchronized(sessionHistoryLock) {
         Iterator var2 = sessionHistoryList.iterator();

         while(var2.hasNext()) {
            Session session = (Session)var2.next();
            if (session.isFFmpeg()) {
               list.add((FFmpegSession)session);
            }
         }

         return list;
      }
   }

   public static List<FFprobeSession> getFFprobeSessions() {
      LinkedList<FFprobeSession> list = new LinkedList();
      synchronized(sessionHistoryLock) {
         Iterator var2 = sessionHistoryList.iterator();

         while(var2.hasNext()) {
            Session session = (Session)var2.next();
            if (session.isFFprobe()) {
               list.add((FFprobeSession)session);
            }
         }

         return list;
      }
   }

   public static List<MediaInformationSession> getMediaInformationSessions() {
      LinkedList<MediaInformationSession> list = new LinkedList();
      synchronized(sessionHistoryLock) {
         Iterator var2 = sessionHistoryList.iterator();

         while(var2.hasNext()) {
            Session session = (Session)var2.next();
            if (session.isMediaInformation()) {
               list.add((MediaInformationSession)session);
            }
         }

         return list;
      }
   }

   public static List<Session> getSessionsByState(SessionState state) {
      LinkedList<Session> list = new LinkedList();
      synchronized(sessionHistoryLock) {
         Iterator var3 = sessionHistoryList.iterator();

         while(var3.hasNext()) {
            Session session = (Session)var3.next();
            if (session.getState() == state) {
               list.add(session);
            }
         }

         return list;
      }
   }

   public static LogRedirectionStrategy getLogRedirectionStrategy() {
      return globalLogRedirectionStrategy;
   }

   public static void setLogRedirectionStrategy(LogRedirectionStrategy logRedirectionStrategy) {
      globalLogRedirectionStrategy = logRedirectionStrategy;
   }

   public static String sessionStateToString(SessionState state) {
      return state.toString();
   }

   public static String[] parseArguments(String command) {
      List<String> argumentList = new ArrayList();
      StringBuilder currentArgument = new StringBuilder();
      boolean singleQuoteStarted = false;
      boolean doubleQuoteStarted = false;

      for(int i = 0; i < command.length(); ++i) {
         Character previousChar;
         if (i > 0) {
            previousChar = command.charAt(i - 1);
         } else {
            previousChar = null;
         }

         char currentChar = command.charAt(i);
         if (currentChar == ' ') {
            if (!singleQuoteStarted && !doubleQuoteStarted) {
               if (currentArgument.length() > 0) {
                  argumentList.add(currentArgument.toString());
                  currentArgument = new StringBuilder();
               }
            } else {
               currentArgument.append(currentChar);
            }
         } else if (currentChar == '\'' && (previousChar == null || previousChar != '\\')) {
            if (singleQuoteStarted) {
               singleQuoteStarted = false;
            } else if (doubleQuoteStarted) {
               currentArgument.append(currentChar);
            } else {
               singleQuoteStarted = true;
            }
         } else if (currentChar != '"' || previousChar != null && previousChar == '\\') {
            currentArgument.append(currentChar);
         } else if (doubleQuoteStarted) {
            doubleQuoteStarted = false;
         } else if (singleQuoteStarted) {
            currentArgument.append(currentChar);
         } else {
            doubleQuoteStarted = true;
         }
      }

      if (currentArgument.length() > 0) {
         argumentList.add(currentArgument.toString());
      }

      return (String[])argumentList.toArray(new String[0]);
   }

   public static String argumentsToString(String[] arguments) {
      if (arguments == null) {
         return "null";
      } else {
         StringBuilder stringBuilder = new StringBuilder();

         for(int i = 0; i < arguments.length; ++i) {
            if (i > 0) {
               stringBuilder.append(" ");
            }

            stringBuilder.append(arguments[i]);
         }

         return stringBuilder.toString();
      }
   }

   private static native void enableNativeRedirection();

   private static native void disableNativeRedirection();

   static native int getNativeLogLevel();

   private static native void setNativeLogLevel(int var0);

   private static native String getNativeFFmpegVersion();

   private static native String getNativeVersion();

   private static native int nativeFFmpegExecute(long var0, String[] var2);

   static native int nativeFFprobeExecute(long var0, String[] var2);

   static native void nativeFFmpegCancel(long var0);

   public static native int messagesInTransmit(long var0);

   private static native int registerNewNativeFFmpegPipe(String var0);

   private static native String getNativeBuildDate();

   private static native int setNativeEnvironmentVariable(String var0, String var1);

   private static native void ignoreNativeSignal(int var0);

   static {
      Exceptions.registerRootPackage("com.arthenica");
      android.util.Log.i("ffmpeg-kit", "Loading ffmpeg-kit.");
      boolean nativeFFmpegTriedAndFailed = NativeLoader.loadFFmpeg();
      Abi.class.getName();
      FFmpegKit.class.getName();
      FFprobeKit.class.getName();
      NativeLoader.loadFFmpegKit(nativeFFmpegTriedAndFailed);
      uniqueIdGenerator = new AtomicInteger(1);
      activeLogLevel = Level.from(NativeLoader.loadLogLevel());
      asyncConcurrencyLimit = 10;
      asyncExecutorService = Executors.newFixedThreadPool(asyncConcurrencyLimit);
      sessionHistorySize = 10;
      sessionHistoryMap = new LinkedHashMap<Long, Session>() {
         protected boolean removeEldestEntry(Entry<Long, Session> eldest) {
            return this.size() > FFmpegKitConfig.sessionHistorySize;
         }
      };
      sessionHistoryList = new LinkedList();
      sessionHistoryLock = new Object();
      globalLogCallback = null;
      globalStatisticsCallback = null;
      globalFFmpegSessionCompleteCallback = null;
      globalFFprobeSessionCompleteCallback = null;
      globalMediaInformationSessionCompleteCallback = null;
      safIdMap = new SparseArray();
      safFileDescriptorMap = new SparseArray();
      globalLogRedirectionStrategy = LogRedirectionStrategy.PRINT_LOGS_WHEN_NO_CALLBACKS_DEFINED;
      android.util.Log.i("ffmpeg-kit", String.format("Loaded ffmpeg-kit-%s-%s-%s-%s.", NativeLoader.loadPackageName(), NativeLoader.loadAbi(), NativeLoader.loadVersion(), NativeLoader.loadBuildDate()));
   }

   static class SAFProtocolUrl {
      private final Integer safId;
      private final Uri uri;
      private final String openMode;
      private final ContentResolver contentResolver;
      private ParcelFileDescriptor parcelFileDescriptor;

      public SAFProtocolUrl(Integer safId, Uri uri, String openMode, ContentResolver contentResolver) {
         this.safId = safId;
         this.uri = uri;
         this.openMode = openMode;
         this.contentResolver = contentResolver;
      }

      public Integer getSafId() {
         return this.safId;
      }

      public Uri getUri() {
         return this.uri;
      }

      public String getOpenMode() {
         return this.openMode;
      }

      public ContentResolver getContentResolver() {
         return this.contentResolver;
      }

      public void setParcelFileDescriptor(ParcelFileDescriptor parcelFileDescriptor) {
         this.parcelFileDescriptor = parcelFileDescriptor;
      }

      public ParcelFileDescriptor getParcelFileDescriptor() {
         return this.parcelFileDescriptor;
      }
   }
}
