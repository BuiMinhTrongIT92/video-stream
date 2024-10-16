package com.stream.video.service;

import com.stream.video.entity.Segment;
import com.stream.video.entity.VideoMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoService {
    private final MongoTemplate mongoTemplate;
    private final M3U8Generator m3U8Generator;
    private final S3Service s3Service;

    @Autowired
    public VideoService(MongoTemplate mongoTemplate, M3U8Generator m3U8Generator, S3Service s3Service) {
        this.mongoTemplate = mongoTemplate;
        this.m3U8Generator = m3U8Generator;
        this.s3Service = s3Service;
    }

    public void saveSegmentToMongo(String videoId, String title, List<String> segmentFilePaths) throws Exception {
        List<Segment> segments = new ArrayList<>();
        for (int i = 0; i < segmentFilePaths.size(); i++) {
            File fileS3 = new File(segmentFilePaths.get(i));
            String s3Url = s3Service.uploadFileToS3(fileS3, fileS3.getName(), title);
            segments.add(new Segment(segmentFilePaths.get(i), i, s3Url));
        }

        VideoMetadata metadata = new VideoMetadata();
        metadata.setVideoId(videoId);
        metadata.setTitle(title);
        metadata.setSegments(segments);
        mongoTemplate.save(metadata);
    }

    public void loadMongo(String name, String directory) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("title").regex(name));
            List<VideoMetadata> videoMetadatas = mongoTemplate.find(query, VideoMetadata.class);
            if (!videoMetadatas.isEmpty()) {
                VideoMetadata videoMetadata = videoMetadatas.get(0);
                List<Segment> segments = videoMetadata.getSegments();
                List<Integer> segmentFilePaths = segments.stream().map(Segment::getSequence).toList();
                m3U8Generator.generateM3U8(directory, segmentFilePaths, name);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }
}
