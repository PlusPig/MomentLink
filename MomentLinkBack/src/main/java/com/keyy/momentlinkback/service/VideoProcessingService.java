package com.keyy.momentlinkback.service;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class VideoProcessingService {

    @Value("${ffmpeg.path:/usr/bin/ffmpeg}")
    private String ffmpegPath;

    @Value("${ffprobe.path:/usr/bin/ffprobe}")
    private String ffprobePath;

    public String processVideo(String videoPath) {
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffprobePath);

            // 输出文件路径
            String outputPath = videoPath.replace(".mp4", "_processed.mp4");

            // 构建FFmpeg命令
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .overrideOutputFiles(true)
                    .addOutput(outputPath)
                    .setFormat("mp4")
                    .setVideoCodec("libx264")
                    .setAudioCodec("aac")
                    .setVideoBitRate(1000000)
                    .setVideoFrameRate(30)
                    .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            log.info("视频处理成功: {}", outputPath);
            return outputPath;

        } catch (IOException e) {
            log.error("视频处理失败", e);
            return videoPath; // 失败时返回原路径
        }
    }

    public String generateThumbnail(String videoPath) {
        try {
            FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
            FFprobe ffprobe = new FFprobe(ffprobePath);

            String thumbnailPath = videoPath.replace(".mp4", "_thumb.jpg");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .overrideOutputFiles(true)
                    .addOutput(thumbnailPath)
                    .setFrames(1)
                    .setVideoFilter("scale=320:-1")
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            log.info("缩略图生成成功: {}", thumbnailPath);
            return thumbnailPath;

        } catch (IOException e) {
            log.error("缩略图生成失败", e);
            return null;
        }
    }
}
