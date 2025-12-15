import 'dart:io';

import 'package:ffmpeg_kit_16kb/ffmpeg_kit.dart';
import 'package:ffmpeg_kit_16kb/log.dart';
import 'package:ffmpeg_kit_16kb/session.dart';
import 'package:ffmpeg_kit_16kb/statistics.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(title: 'FFmpeg example'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  String logString = 'Logs will be here.';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Text(logString, style: TextStyle(fontSize: 16)),
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: executeFFmpegCommand,
        child: const Icon(Icons.rocket),
      ),
    );
  }

  void executeFFmpegCommand() async {
    logString = '';
    final tempDir = await getTemporaryDirectory();
    final sampleVideoRoot = await rootBundle.load('assets/sample_video.mp4');
    final sampleVideoFile = File('${tempDir.path}/sample_video.mp4');
    final outputFile = File('${tempDir.path}/output.mp4');
    await sampleVideoFile.writeAsBytes(sampleVideoRoot.buffer.asUint8List());
    if (outputFile.existsSync()) await outputFile.delete();

    /// Execute FFmpeg command
    await FFmpegKit.executeAsync(
      '-i '
      '${sampleVideoFile.path} -c:v mpeg4 '
      '${tempDir.path}/output.mp4',
      (Session session) async {
        debugPrint('session: ${await session.getOutput()}');
      },
      (Log log) {
        logString += log.getMessage();
        debugPrint('log: ${log.getMessage()}');
      },
      (Statistics statistics) {
        debugPrint('statistics: ${statistics.getSize()}');
      },
    );
    setState(() {});
  }
}
