package com.stream.video.service;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class VideoProcessingService {

    public void processVideo(String inputFilePath, String outputDir, String videoName) {
        try {
            log.info("inputFilePath: {}, outputDir: {}, videoName: {}", inputFilePath, outputDir, videoName);
            Path inputVideoPath = Paths.get(inputFilePath);
            log.info("inputVideoPath: {}", inputVideoPath);
            Path outputDirectory = Paths.get(outputDir);
            Path outputPlaylist = outputDirectory.resolve(videoName.concat(".mp4"));

            // Run FFmpeg command to segment video
            FFmpegResult result = FFmpeg.atPath()
                    .addInput(UrlInput.fromPath(inputVideoPath))
                    .addOutput(
                            UrlOutput.toPath(outputPlaylist)
                                    .setFormat("hls")
                                    .addArguments("-hls_time", "10")    // Segments of 10 seconds
                                    .addArguments("-hls_list_size", "0") // Store all segments
                    )
                    .setOverwriteOutput(true)
                    .execute();

        } catch (Exception e) {
            log.error("Error while processing video", e);
        }

    }
}
