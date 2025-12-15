package com.arthenica.ffmpegkit;

public enum Level {
   AV_LOG_STDERR(-16),
   AV_LOG_QUIET(-8),
   AV_LOG_PANIC(0),
   AV_LOG_FATAL(8),
   AV_LOG_ERROR(16),
   AV_LOG_WARNING(24),
   AV_LOG_INFO(32),
   AV_LOG_VERBOSE(40),
   AV_LOG_DEBUG(48),
   AV_LOG_TRACE(56);

   private final int value;

   public static Level from(int value) {
      if (value == AV_LOG_STDERR.getValue()) {
         return AV_LOG_STDERR;
      } else if (value == AV_LOG_QUIET.getValue()) {
         return AV_LOG_QUIET;
      } else if (value == AV_LOG_PANIC.getValue()) {
         return AV_LOG_PANIC;
      } else if (value == AV_LOG_FATAL.getValue()) {
         return AV_LOG_FATAL;
      } else if (value == AV_LOG_ERROR.getValue()) {
         return AV_LOG_ERROR;
      } else if (value == AV_LOG_WARNING.getValue()) {
         return AV_LOG_WARNING;
      } else if (value == AV_LOG_INFO.getValue()) {
         return AV_LOG_INFO;
      } else if (value == AV_LOG_VERBOSE.getValue()) {
         return AV_LOG_VERBOSE;
      } else {
         return value == AV_LOG_DEBUG.getValue() ? AV_LOG_DEBUG : AV_LOG_TRACE;
      }
   }

   public int getValue() {
      return this.value;
   }

   private Level(int value) {
      this.value = value;
   }

   // $FF: synthetic method
   private static Level[] $values() {
      return new Level[]{AV_LOG_STDERR, AV_LOG_QUIET, AV_LOG_PANIC, AV_LOG_FATAL, AV_LOG_ERROR, AV_LOG_WARNING, AV_LOG_INFO, AV_LOG_VERBOSE, AV_LOG_DEBUG, AV_LOG_TRACE};
   }
}
