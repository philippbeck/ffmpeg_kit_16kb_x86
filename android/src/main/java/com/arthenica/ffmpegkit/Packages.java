package com.arthenica.ffmpegkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Packages {
   private static final List<String> supportedExternalLibraries = new ArrayList();

   public static String getPackageName() {
      List<String> externalLibraryList = getExternalLibraries();
      boolean speex = externalLibraryList.contains("speex");
      boolean fribidi = externalLibraryList.contains("fribidi");
      boolean gnutls = externalLibraryList.contains("gnutls");
      boolean xvid = externalLibraryList.contains("xvid");
      boolean minGpl = false;
      boolean https = false;
      boolean httpsGpl = false;
      boolean audio = false;
      boolean video = false;
      boolean full = false;
      boolean fullGpl = false;
      if (speex && fribidi) {
         if (xvid) {
            fullGpl = true;
         } else {
            full = true;
         }
      } else if (speex) {
         audio = true;
      } else if (fribidi) {
         video = true;
      } else if (xvid) {
         if (gnutls) {
            httpsGpl = true;
         } else {
            minGpl = true;
         }
      } else if (gnutls) {
         https = true;
      }

      if (fullGpl) {
         return externalLibraryList.contains("dav1d") && externalLibraryList.contains("fontconfig") && externalLibraryList.contains("freetype") && externalLibraryList.contains("fribidi") && externalLibraryList.contains("gmp") && externalLibraryList.contains("gnutls") && externalLibraryList.contains("kvazaar") && externalLibraryList.contains("mp3lame") && externalLibraryList.contains("libass") && externalLibraryList.contains("iconv") && externalLibraryList.contains("libilbc") && externalLibraryList.contains("libtheora") && externalLibraryList.contains("libvidstab") && externalLibraryList.contains("libvorbis") && externalLibraryList.contains("libvpx") && externalLibraryList.contains("libwebp") && externalLibraryList.contains("libxml2") && externalLibraryList.contains("opencore-amr") && externalLibraryList.contains("opus") && externalLibraryList.contains("shine") && externalLibraryList.contains("snappy") && externalLibraryList.contains("soxr") && externalLibraryList.contains("speex") && externalLibraryList.contains("twolame") && externalLibraryList.contains("x264") && externalLibraryList.contains("x265") && externalLibraryList.contains("xvid") && externalLibraryList.contains("zimg") ? "full-gpl" : "custom";
      } else if (full) {
         return externalLibraryList.contains("dav1d") && externalLibraryList.contains("fontconfig") && externalLibraryList.contains("freetype") && externalLibraryList.contains("fribidi") && externalLibraryList.contains("gmp") && externalLibraryList.contains("gnutls") && externalLibraryList.contains("kvazaar") && externalLibraryList.contains("mp3lame") && externalLibraryList.contains("libass") && externalLibraryList.contains("iconv") && externalLibraryList.contains("libilbc") && externalLibraryList.contains("libtheora") && externalLibraryList.contains("libvorbis") && externalLibraryList.contains("libvpx") && externalLibraryList.contains("libwebp") && externalLibraryList.contains("libxml2") && externalLibraryList.contains("opencore-amr") && externalLibraryList.contains("opus") && externalLibraryList.contains("shine") && externalLibraryList.contains("snappy") && externalLibraryList.contains("soxr") && externalLibraryList.contains("speex") && externalLibraryList.contains("twolame") && externalLibraryList.contains("zimg") ? "full" : "custom";
      } else if (video) {
         return externalLibraryList.contains("dav1d") && externalLibraryList.contains("fontconfig") && externalLibraryList.contains("freetype") && externalLibraryList.contains("fribidi") && externalLibraryList.contains("kvazaar") && externalLibraryList.contains("libass") && externalLibraryList.contains("iconv") && externalLibraryList.contains("libtheora") && externalLibraryList.contains("libvpx") && externalLibraryList.contains("libwebp") && externalLibraryList.contains("snappy") && externalLibraryList.contains("zimg") ? "video" : "custom";
      } else if (audio) {
         return externalLibraryList.contains("mp3lame") && externalLibraryList.contains("libilbc") && externalLibraryList.contains("libvorbis") && externalLibraryList.contains("opencore-amr") && externalLibraryList.contains("opus") && externalLibraryList.contains("shine") && externalLibraryList.contains("soxr") && externalLibraryList.contains("speex") && externalLibraryList.contains("twolame") ? "audio" : "custom";
      } else if (httpsGpl) {
         return externalLibraryList.contains("gmp") && externalLibraryList.contains("gnutls") && externalLibraryList.contains("libvidstab") && externalLibraryList.contains("x264") && externalLibraryList.contains("x265") && externalLibraryList.contains("xvid") ? "https-gpl" : "custom";
      } else if (https) {
         return externalLibraryList.contains("gmp") && externalLibraryList.contains("gnutls") ? "https" : "custom";
      } else if (minGpl) {
         return externalLibraryList.contains("libvidstab") && externalLibraryList.contains("x264") && externalLibraryList.contains("x265") && externalLibraryList.contains("xvid") ? "min-gpl" : "custom";
      } else {
         return externalLibraryList.size() == 0 ? "min" : "custom";
      }
   }

   public static List<String> getExternalLibraries() {
      String buildConfiguration = AbiDetect.getNativeBuildConf();
      List<String> enabledLibraryList = new ArrayList();
      Iterator var2 = supportedExternalLibraries.iterator();

      while(true) {
         String supportedExternalLibrary;
         do {
            if (!var2.hasNext()) {
               Collections.sort(enabledLibraryList);
               return enabledLibraryList;
            }

            supportedExternalLibrary = (String)var2.next();
         } while(!buildConfiguration.contains("enable-" + supportedExternalLibrary) && !buildConfiguration.contains("enable-lib" + supportedExternalLibrary));

         enabledLibraryList.add(supportedExternalLibrary);
      }
   }

   static {
      supportedExternalLibraries.add("dav1d");
      supportedExternalLibraries.add("fontconfig");
      supportedExternalLibraries.add("freetype");
      supportedExternalLibraries.add("fribidi");
      supportedExternalLibraries.add("gmp");
      supportedExternalLibraries.add("gnutls");
      supportedExternalLibraries.add("kvazaar");
      supportedExternalLibraries.add("mp3lame");
      supportedExternalLibraries.add("libass");
      supportedExternalLibraries.add("iconv");
      supportedExternalLibraries.add("libilbc");
      supportedExternalLibraries.add("libtheora");
      supportedExternalLibraries.add("libvidstab");
      supportedExternalLibraries.add("libvorbis");
      supportedExternalLibraries.add("libvpx");
      supportedExternalLibraries.add("libwebp");
      supportedExternalLibraries.add("libxml2");
      supportedExternalLibraries.add("opencore-amr");
      supportedExternalLibraries.add("openh264");
      supportedExternalLibraries.add("openssl");
      supportedExternalLibraries.add("opus");
      supportedExternalLibraries.add("rubberband");
      supportedExternalLibraries.add("sdl2");
      supportedExternalLibraries.add("shine");
      supportedExternalLibraries.add("snappy");
      supportedExternalLibraries.add("soxr");
      supportedExternalLibraries.add("speex");
      supportedExternalLibraries.add("srt");
      supportedExternalLibraries.add("tesseract");
      supportedExternalLibraries.add("twolame");
      supportedExternalLibraries.add("x264");
      supportedExternalLibraries.add("x265");
      supportedExternalLibraries.add("xvid");
      supportedExternalLibraries.add("zimg");
   }
}
