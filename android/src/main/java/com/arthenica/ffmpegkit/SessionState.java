package com.arthenica.ffmpegkit;

public enum SessionState {
   CREATED,
   RUNNING,
   FAILED,
   COMPLETED;

   // $FF: synthetic method
   private static SessionState[] $values() {
      return new SessionState[]{CREATED, RUNNING, FAILED, COMPLETED};
   }
}
