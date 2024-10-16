package com.stream.video.service;

import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Service
public class VideoStorageService {

    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public VideoStorageService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public ObjectId storeSegment(String filePath, String fileName) {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            GridFSUploadOptions options = new GridFSUploadOptions().metadata(null);
            return gridFsTemplate.store(inputStream, fileName, options);
        } catch (IOException e) {
            log.error("Error while storing segment", e);
        }
        return null;
    }
}
