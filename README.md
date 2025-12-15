# FFmpeg Kit 16KB with x86_64 Support

Fork of [ffmpeg_kit_16kb](https://github.com/zhouxin1233/ffmpeg_kit_16kb) that adds x86_64 emulator support.

## Changes from original

- Uses Maven dependency `com.moizhassan.ffmpeg:ffmpeg-kit-16kb:6.0.0` instead of bundled .so files
- Supports all architectures: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`
- 16KB page size aligned for Android 15+ (API 35+) Play Store compliance

## Installation

Add as a Git dependency in your `pubspec.yaml`:

```yaml
dependencies:
  ffmpeg_kit_16kb_x86:
    git:
      url: https://github.com/philippbeck/ffmpeg_kit_16kb_x86.git
      ref: main
```

## Features

- FFmpeg v6.0 with 16KB page size support
- All Android architectures (ARM for devices + x86/x86_64 for emulators)
- Android API Level 24 or later
- iOS and macOS support
- Licensed under `LGPL 3.0` by default, some packages under `GPL v3.0`

## Credits

- [ffmpeg_kit_16kb](https://github.com/zhouxin1233/ffmpeg_kit_16kb) by zhouxin1233
- [ffmpeg-kit-android-16KB](https://github.com/moizhassankh/ffmpeg-kit-android-16KB) by moizhassankh
- [ffmpeg-kit](https://github.com/arthenica/ffmpeg-kit) by arthenica

---

## Usage

#### 4. Platform Support

The following table shows Android API level, iOS deployment target and macOS deployment target requirements in  
`ffmpeg_kit_16kb` releases.

<table align="center">  
  <thead>  
    <tr>  
      <th align="center" colspan="3">LTS Release</th>  
    </tr>  
    <tr>  
      <th align="center">Android<br>API Level</th>  
      <th align="center">iOS Minimum<br>Deployment Target</th>  
      <th align="center">macOS Minimum<br>Deployment Target</th>  
    </tr>  
  </thead>  
  <tbody>  
    <tr>  
      <td align="center">24</td>  
      <td align="center">14</td>  
      <td align="center">10.15</td>  
    </tr>  
  </tbody>  
</table>  

### 3. Using

1. Execute FFmpeg commands.

```dart  
import 'package:ffmpeg_kit_16kb/ffmpeg_kit.dart';

FFmpegKit.execute('-i file1.mp4 -c:v mpeg4 file2.mp4').then((session) async {
    final returnCode = await session.getReturnCode();  
    if (ReturnCode.isSuccess(returnCode)) {  
    // SUCCESS  
    } else if (ReturnCode.isCancel(returnCode)) {  
    // CANCEL  
    } else {  
    // ERROR  
    }
});
```  
2. Each `execute` call creates a new session. Access every detail about your execution from the session created.  
  
```dart  
FFmpegKit.execute('-i file1.mp4 -c:v mpeg4 file2.mp4').then((session) async {  
    // Unique session id created for this execution
    final sessionId = session.getSessionId();  
    // Command arguments as a single string
    final command = session.getCommand();  
    // Command arguments
    final commandArguments = session.getArguments();  
    // State of the execution. Shows whether it is still running or completed
    final state = await session.getState();  
    // Return code for completed sessions. Will be undefined if session is still running or FFmpegKit fails to run it
    final returnCode = await session.getReturnCode();  
    final startTime = session.getStartTime();
    final endTime = await session.getEndTime();
    final duration = await session.getDuration();  
    // Console output generated for this execution
    final output = await session.getOutput();  
    // The stack trace if FFmpegKit fails to run a command
    final failStackTrace = await session.getFailStackTrace();  
    // The list of logs generated for this execution
    final logs = await session.getLogs();  
    // The list of statistics generated for this execution (only available on FFmpegSession)
    final statistics = await (session as FFmpegSession).getStatistics();  
});
```  
3. Execute `FFmpeg` commands by providing session specific `execute`/`log`/`session` callbacks.

```dart  
FFmpegKit.executeAsync('-i file1.mp4 -c:v mpeg4 file2.mp4', (Session session) async {
    // CALLED WHEN SESSION IS EXECUTED  
}, (Log log) {  
    // CALLED WHEN SESSION PRINTS LOGS  
}, (Statistics statistics) {  
    // CALLED WHEN SESSION GENERATES STATISTICS  
});
```  
4. Execute `FFprobe` commands.  
  
```dart  
FFprobeKit.execute(ffprobeCommand).then((session) async {  
    // CALLED WHEN SESSION IS EXECUTED  
});  
```  
5. Get media information for a file/url.

```dart  
FFprobeKit.getMediaInformation('<file path or url>').then((session) async {  
    final information = await session.getMediaInformation();  
    if (information == null) {  
        // CHECK THE FOLLOWING ATTRIBUTES ON ERROR
        final state = FFmpegKitConfig.sessionStateToString(await session.getState());
        final returnCode = await session.getReturnCode();
        final failStackTrace = await session.getFailStackTrace();
        final duration = await session.getDuration();
        final output = await session.getOutput();
    }
});
```  
6. Stop ongoing FFmpeg operations.  
  
- Stop all sessions  
```dart  
FFmpegKit.cancel();
```
- Stop a specific session  
```dart  
FFmpegKit.cancel(sessionId);  
```  
7. (Android) Convert Storage Access Framework (SAF) Uris into paths that can be read or written by  
   `FFmpegKit` and `FFprobeKit`.

- Reading a file:
```dart  
FFmpegKitConfig.selectDocumentForRead('*/*').then((uri) {  
    FFmpegKitConfig.getSafParameterForRead(uri!).then((safUrl) {
        FFmpegKit.executeAsync("-i ${safUrl!} -c:v mpeg4 file2.mp4");
    });
});
```  
- Writing to a file:  
```dart  
FFmpegKitConfig.selectDocumentForWrite('video.mp4', 'video/*').then((uri) {
    FFmpegKitConfig.getSafParameterForWrite(uri!).then((safUrl) {
        FFmpegKit.executeAsync("-i file1.mp4 -c:v mpeg4 ${safUrl}");
    });
});  
```  
8. Get previous `FFmpeg`, `FFprobe` and `MediaInformation` sessions from the session history.

```dart  
FFmpegKit.listSessions().then((sessionList) {  
    sessionList.forEach((session) {
        final sessionId = session.getSessionId();
    });
});  
FFprobeKit.listFFprobeSessions().then((sessionList) {
    sessionList.forEach((session) {
        final sessionId = session.getSessionId();
    });
});  
FFprobeKit.listMediaInformationSessions().then((sessionList) {
    sessionList.forEach((session) {
        final sessionId = session.getSessionId();
    });
});
```  
9. Enable global callbacks.  
  
- Session type specific Complete Callbacks, called when an async session has been completed  
  
```dart  
FFmpegKitConfig.enableFFmpegSessionCompleteCallback((session) {
    final sessionId = session.getSessionId();
});  
FFmpegKitConfig.enableFFprobeSessionCompleteCallback((session) {
    final sessionId = session.getSessionId();
});  
FFmpegKitConfig.enableMediaInformationSessionCompleteCallback((session) {
    final sessionId = session.getSessionId();
});  
```  
- Log Callback, called when a session generates logs

```dart  
FFmpegKitConfig.enableLogCallback((log) {  
    final message = log.getMessage();
});
```  
- Statistics Callback, called when a session generates statistics  
  
```dart  
FFmpegKitConfig.enableStatisticsCallback((statistics) {  
    final size = statistics.getSize();
});  
```  
10. Register system fonts and custom font directories.

```dart  
FFmpegKitConfig.setFontDirectoryList(["/system/fonts", "/System/Library/Fonts", "<folder with fonts>"]);
```
