package com.arthenica.ffmpegkit;

import com.arthenica.smartexception.java9.Exceptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MediaInformationJsonParser {
   public static final String KEY_STREAMS = "streams";
   public static final String KEY_CHAPTERS = "chapters";

   public static MediaInformation from(String ffprobeJsonOutput) {
      try {
         return fromWithError(ffprobeJsonOutput);
      } catch (JSONException var2) {
         android.util.Log.e("ffmpeg-kit", String.format("MediaInformation parsing failed.%s", Exceptions.getStackTraceString(var2)));
         return null;
      }
   }

   public static MediaInformation fromWithError(String ffprobeJsonOutput) throws JSONException {
      JSONObject jsonObject = new JSONObject(ffprobeJsonOutput);
      JSONArray streamArray = jsonObject.optJSONArray("streams");
      JSONArray chapterArray = jsonObject.optJSONArray("chapters");
      ArrayList<StreamInformation> streamList = new ArrayList();

      for(int i = 0; streamArray != null && i < streamArray.length(); ++i) {
         JSONObject streamObject = streamArray.optJSONObject(i);
         if (streamObject != null) {
            streamList.add(new StreamInformation(streamObject));
         }
      }

      ArrayList<Chapter> chapterList = new ArrayList();

      for(int i = 0; chapterArray != null && i < chapterArray.length(); ++i) {
         JSONObject chapterObject = chapterArray.optJSONObject(i);
         if (chapterObject != null) {
            chapterList.add(new Chapter(chapterObject));
         }
      }

      return new MediaInformation(jsonObject, streamList, chapterList);
   }
}
