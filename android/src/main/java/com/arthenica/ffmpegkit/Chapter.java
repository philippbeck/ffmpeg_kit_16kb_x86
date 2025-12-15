package com.arthenica.ffmpegkit;

import org.json.JSONObject;

public class Chapter {
   public static final String KEY_ID = "id";
   public static final String KEY_TIME_BASE = "time_base";
   public static final String KEY_START = "start";
   public static final String KEY_START_TIME = "start_time";
   public static final String KEY_END = "end";
   public static final String KEY_END_TIME = "end_time";
   public static final String KEY_TAGS = "tags";
   private final JSONObject jsonObject;

   public Chapter(JSONObject jsonObject) {
      this.jsonObject = jsonObject;
   }

   public Long getId() {
      return this.getNumberProperty("id");
   }

   public String getTimeBase() {
      return this.getStringProperty("time_base");
   }

   public Long getStart() {
      return this.getNumberProperty("start");
   }

   public String getStartTime() {
      return this.getStringProperty("start_time");
   }

   public Long getEnd() {
      return this.getNumberProperty("end");
   }

   public String getEndTime() {
      return this.getStringProperty("end_time");
   }

   public JSONObject getTags() {
      return this.getProperty("tags");
   }

   public String getStringProperty(String key) {
      JSONObject allProperties = this.getAllProperties();
      if (allProperties == null) {
         return null;
      } else {
         return allProperties.has(key) ? allProperties.optString(key) : null;
      }
   }

   public Long getNumberProperty(String key) {
      JSONObject allProperties = this.getAllProperties();
      if (allProperties == null) {
         return null;
      } else {
         return allProperties.has(key) ? allProperties.optLong(key) : null;
      }
   }

   public JSONObject getProperty(String key) {
      JSONObject allProperties = this.getAllProperties();
      return allProperties == null ? null : allProperties.optJSONObject(key);
   }

   public JSONObject getAllProperties() {
      return this.jsonObject;
   }
}
