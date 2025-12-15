package com.arthenica.ffmpegkit;

import org.json.JSONObject;

import java.util.List;

public class MediaInformation {
   public static final String KEY_FORMAT_PROPERTIES = "format";
   public static final String KEY_FILENAME = "filename";
   public static final String KEY_FORMAT = "format_name";
   public static final String KEY_FORMAT_LONG = "format_long_name";
   public static final String KEY_START_TIME = "start_time";
   public static final String KEY_DURATION = "duration";
   public static final String KEY_SIZE = "size";
   public static final String KEY_BIT_RATE = "bit_rate";
   public static final String KEY_TAGS = "tags";
   private final JSONObject jsonObject;
   private final List<StreamInformation> streams;
   private final List<Chapter> chapters;

   public MediaInformation(JSONObject jsonObject, List<StreamInformation> streams, List<Chapter> chapters) {
      this.jsonObject = jsonObject;
      this.streams = streams;
      this.chapters = chapters;
   }

   public String getFilename() {
      return this.getStringFormatProperty("filename");
   }

   public String getFormat() {
      return this.getStringFormatProperty("format_name");
   }

   public String getLongFormat() {
      return this.getStringFormatProperty("format_long_name");
   }

   public String getDuration() {
      return this.getStringFormatProperty("duration");
   }

   public String getStartTime() {
      return this.getStringFormatProperty("start_time");
   }

   public String getSize() {
      return this.getStringFormatProperty("size");
   }

   public String getBitrate() {
      return this.getStringFormatProperty("bit_rate");
   }

   public JSONObject getTags() {
      return this.getFormatProperty("tags");
   }

   public List<StreamInformation> getStreams() {
      return this.streams;
   }

   public List<Chapter> getChapters() {
      return this.chapters;
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

   public String getStringFormatProperty(String key) {
      JSONObject formatProperties = this.getFormatProperties();
      if (formatProperties == null) {
         return null;
      } else {
         return formatProperties.has(key) ? formatProperties.optString(key) : null;
      }
   }

   public Long getNumberFormatProperty(String key) {
      JSONObject formatProperties = this.getFormatProperties();
      if (formatProperties == null) {
         return null;
      } else {
         return formatProperties.has(key) ? formatProperties.optLong(key) : null;
      }
   }

   public JSONObject getFormatProperty(String key) {
      JSONObject formatProperties = this.getFormatProperties();
      return formatProperties == null ? null : formatProperties.optJSONObject(key);
   }

   public JSONObject getFormatProperties() {
      return this.jsonObject.optJSONObject("format");
   }

   public JSONObject getAllProperties() {
      return this.jsonObject;
   }
}
