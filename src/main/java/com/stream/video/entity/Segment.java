package com.stream.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("video_segments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Segment {
    private String fileId;
    private int sequence;
    private String s3Url;
}
