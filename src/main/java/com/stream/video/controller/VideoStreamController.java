package com.stream.video.controller;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.stream.video.config.AWSS3;
import com.stream.video.service.S3Service;
import com.stream.video.service.VideoProcessingService;
import com.stream.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class VideoStreamController {
    private final VideoProcessingService videoProcessingService;
    private final VideoService videoService;
    String currentPath = System.getProperty("user.dir");
    private final String videoPath = currentPath.concat("/ubuntu");
    private final AWSS3 amazonS3;
    private final S3Service s3Service;
    @Value("${cloud.aws.region.static}")
    private String region;
    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public VideoStreamController(VideoProcessingService videoProcessingService, VideoService videoService,
                                 AWSS3 amazonS3, S3Service s3Service) {
        this.videoProcessingService = videoProcessingService;
        this.videoService = videoService;
        this.amazonS3 = amazonS3;
        this.s3Service = s3Service;
    }

    @GetMapping("/stream/{name}")
    @Description("Create m3u8 file")
    public ResponseEntity<String> streamVideo(@PathVariable String name) {
        log.info("Begin Create m3u8 file");
        try {
            String direction = videoPath.substring(0, videoPath.lastIndexOf("/"));
            //save to local direction
            videoService.loadMongo(name, direction);
            s3Service.uploadFileToS3M3U8(direction.concat("/").concat(name).concat(".m3u8"), name, name);
            return ResponseEntity.ok().body("OK");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/file")
    @Description("Upload file")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Begin upload file: ");
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
        }
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            log.info("tempDir: {}", tempDir);
            Path tempFilePath = Paths.get(tempDir, file.getOriginalFilename());
            Files.write(tempFilePath, file.getBytes());
            String outPath = videoPath.concat("/").concat("").concat(file.getOriginalFilename().replace(".mp4", ""));
            log.info("currentPath: {}", currentPath);
            log.info("VideoPath: {}", videoPath);
            String videoName = file.getOriginalFilename().replace(".mp4", "");
            File directory = new File(outPath);
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("Directory created successfully!");
                } else {
                    System.out.println("Failed to create directory.");
                }
            } else {
                System.out.println("Directory already exists.");
            }
            //Make folder
            videoProcessingService.processVideo(tempFilePath.toAbsolutePath().toString(), outPath, videoName);
            // Xóa file tạm sau khi hoàn thành
            {
                log.info("delete tempFile url: {}", tempFilePath.toAbsolutePath().toString());
                Files.delete(tempFilePath);
            }
            //save segment to mongo
            List<Path> tsFiles = new ArrayList<>();
            List<Path> mp4TmpFiles = new ArrayList<>();
            log.info("videoPath: {}", videoPath);
            Files.walkFileTree(Paths.get(videoPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    // Kiểm tra nếu file có đuôi .ts
                    if (file.toString().endsWith(".ts")) {
                        tsFiles.add(file);
                    }
                    if (file.toString().endsWith(".mp4")) {
                        mp4TmpFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            List<String> segmentFilePaths = tsFiles.stream().map(Path::toString).sorted(((o1, o2) -> {
                int o1Name = Integer.parseInt(o1.substring(o1.lastIndexOf(videoName), o1.length() - 3).replace(videoName, ""));
                int o2Name = Integer.parseInt(o2.substring(o2.lastIndexOf(videoName), o2.length() - 3).replace(videoName, ""));
                return o1Name - o2Name;
            })).toList();
            try {
                videoService.saveSegmentToMongo(UUID.randomUUID().toString(), videoName, segmentFilePaths);
            } catch (Exception e) {
                log.info("Error when save segment to mongo {}", e.getMessage());
            }
            try {
                log.info("Delete .ts and .mp4");
                tsFiles.forEach(item -> {
                    try {
                        Files.delete(Path.of(item.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                mp4TmpFiles.forEach(item -> {
                    try {
                        Files.delete(Path.of(item.toString()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                Files.delete(Path.of(outPath));
            } catch (Exception e) {
                log.info("Error when delete .ts and .mp4 {}", e.getMessage());
            }
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not upload file");
        }
    }

    @GetMapping("/{name}/{segment}")
    @Description("load ts file")
    public ResponseEntity<Resource> getVideoSegment(@PathVariable String name, @PathVariable String segment) throws IOException {
        log.info("Begin load ts file");
        String path = name.concat("/").concat(name).concat(segment).concat(".ts");
        log.info(path);

        //This load from local
//        File file = new File(path);
//        Resource resource = new FileSystemResource(file);
//        return ResponseEntity.ok().contentType(MediaType.valueOf("video/mp2t")).body(resource);


//        This load from S3
        S3ObjectInputStream s3is = s3Service.getFileFromS3(path);
        InputStreamResource resource = new InputStreamResource(s3is);

        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + path);

        // Return the ResponseEntity with the video content
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.valueOf("video/mp2t"))
                .body(resource);
    }

    @GetMapping("/m3u8/{name}")
    @Description("load m3u8 file")
    public ResponseEntity<Resource> getVideoSegment(@PathVariable String name) throws IOException {
        log.info("Begin load m3u8 file");
        //This load from local
//        String path = String.format("/Users/trongbui/Desktop/%s/%s.m3u8", name, name);
//        log.info("Serving file from path: " + path);
//        File file = new File(path);
//        Resource resource = new FileSystemResource(file);


        //This load from S3
        // Convert S3 Object InputStream to InputStreamResource
        String s3FilePath = name.concat("/") + name;
        S3ObjectInputStream s3is = s3Service.getFileFromS3(s3FilePath);
        InputStreamResource resource = new InputStreamResource(s3is);

        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + s3FilePath);

        // Return the ResponseEntity with the video content
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.valueOf("application/vnd.apple.mpegurl"))
                .body(resource);
    }
}
