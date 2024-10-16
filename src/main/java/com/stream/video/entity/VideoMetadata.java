package com.stream.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {

    @Id
    private String videoId;
    private String title;
    private List<Segment> segments;
    private String playlistM3U8;
}
