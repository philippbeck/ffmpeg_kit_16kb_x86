package com.arthenica.ffmpegkit;

import org.json.JSONObject;

public class StreamInformation {
   public static final String KEY_INDEX = "index";
   public static final String KEY_TYPE = "codec_type";
   public static final String KEY_CODEC = "codec_name";
   public static final String KEY_CODEC_LONG = "codec_long_name";
   public static final String KEY_FORMAT = "pix_fmt";
   public static final String KEY_WIDTH = "width";
   public static final String KEY_HEIGHT = "height";
   public static final String KEY_BIT_RATE = "bit_rate";
   public static final String KEY_SAMPLE_RATE = "sample_rate";
   public static final String KEY_SAMPLE_FORMAT = "sample_fmt";
   public static final String KEY_CHANNEL_LAYOUT = "channel_layout";
   public static final String KEY_SAMPLE_ASPECT_RATIO = "sample_aspect_ratio";
   public static final String KEY_DISPLAY_ASPECT_RATIO = "display_aspect_ratio";
   public static final String KEY_AVERAGE_FRAME_RATE = "avg_frame_rate";
   public static final String KEY_REAL_FRAME_RATE = "r_frame_rate";
   public static final String KEY_TIME_BASE = "time_base";
   public static final String KEY_CODEC_TIME_BASE = "codec_time_base";
   public static final String KEY_TAGS = "tags";
   private final JSONObject jsonObject;

   public StreamInformation(JSONObject jsonObject) {
      this.jsonObject = jsonObject;
   }

   public Long getIndex() {
      return this.getNumberProperty("index");
   }

   public String getType() {
      return this.getStringProperty("codec_type");
   }

   public String getCodec() {
      return this.getStringProperty("codec_name");
   }

   public String getCodecLong() {
      return this.getStringProperty("codec_long_name");
   }

   public String getFormat() {
      return this.getStringProperty("pix_fmt");
   }

   public Long getWidth() {
      return this.getNumberProperty("width");
   }

   public Long getHeight() {
      return this.getNumberProperty("height");
   }

   public String getBitrate() {
      return this.getStringProperty("bit_rate");
   }

   public String getSampleRate() {
      return this.getStringProperty("sample_rate");
   }

   public String getSampleFormat() {
      return this.getStringProperty("sample_fmt");
   }

   public String getChannelLayout() {
      return this.getStringProperty("channel_layout");
   }

   public String getSampleAspectRatio() {
      return this.getStringProperty("sample_aspect_ratio");
   }

   public String getDisplayAspectRatio() {
      return this.getStringProperty("display_aspect_ratio");
   }

   public String getAverageFrameRate() {
      return this.getStringProperty("avg_frame_rate");
   }

   public String getRealFrameRate() {
      return this.getStringProperty("r_frame_rate");
   }

   public String getTimeBase() {
      return this.getStringProperty("time_base");
   }

   public String getCodecTimeBase() {
      return this.getStringProperty("codec_time_base");
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
