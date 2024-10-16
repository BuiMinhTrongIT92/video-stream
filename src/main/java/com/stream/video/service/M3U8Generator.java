package com.stream.video.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class M3U8Generator {
    private String url;
    private final EC2InstanceService ec2InstanceService;

    @Autowired
    public M3U8Generator(EC2InstanceService ec2InstanceService) {
        this.ec2InstanceService = ec2InstanceService;
        this.url = String.format("http://%s:8089/videostream/", ec2InstanceService.getIpV4Instance().replace("\n", ""));
    }

    public void generateM3U8(String outputFilePath, List<Integer> segments, String name) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath.concat("/").concat(name).concat(".m3u8")))) {
            writer.write("#EXTM3U\n");
            writer.write("#EXT-X-VERSION:3\n");
            writer.write("#EXT-X-ALLOW-CACHE:YES\n");
            writer.write("#EXT-X-TARGETDURATION:10\n");
            writer.write("#EXT-X-MEDIA-SEQUENCE:0\n\n");

            for (Object segment : segments) {
                writer.write("#EXTINF:10.0,\n"); // Thay đổi thời gian nếu cần
                writer.write(url + name + "/" + segment + "\n");
            }

            writer.write("#EXT-X-ENDLIST\n");
        } catch (FileNotFoundException e) {
            log.error("FileNotFoundException while generating M3U8 file", e);
        } catch (IOException e) {
            log.error("IOException while generating M3U8 file", e);
        }
    }
}
