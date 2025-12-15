package com.arthenica.ffmpegkit;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build.VERSION;

import java.util.ArrayList;
import java.util.List;

class CameraSupport {
   static List<String> extractSupportedCameraIds(Context context) {
      List<String> detectedCameraIdList = new ArrayList();
      if (VERSION.SDK_INT >= 24) {
         try {
            CameraManager manager = (CameraManager)context.getSystemService("camera");
            if (manager != null) {
               String[] cameraIdList = manager.getCameraIdList();
               String[] var4 = cameraIdList;
               int var5 = cameraIdList.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  String cameraId = var4[var6];
                  CameraCharacteristics chars = manager.getCameraCharacteristics(cameraId);
                  Integer cameraSupport = (Integer)chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                  if (cameraSupport != null && cameraSupport == 2) {
                     android.util.Log.d("ffmpeg-kit", "Detected camera with id " + cameraId + " has LEGACY hardware level which is not supported by Android Camera2 NDK API.");
                  } else if (cameraSupport != null) {
                     detectedCameraIdList.add(cameraId);
                  }
               }
            }
         } catch (CameraAccessException var10) {
            android.util.Log.w("ffmpeg-kit", "Detecting camera ids failed.", var10);
         }
      }

      return detectedCameraIdList;
   }
}
