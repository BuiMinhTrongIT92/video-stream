package com.stream.video.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    public static String calculateMD5(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            fis.close();

            byte[] md5Bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadFileToS3M3U8(String localFilePath, String fileName, String title) {
        try {
            File file = new File(localFilePath);

            // Đường dẫn trên S3 (thư mục "video" và tên file)
            String s3FilePath = title.concat("/") + fileName;

            // Tải file lên S3
            amazonS3.putObject(new PutObjectRequest(bucketName, s3FilePath, file));
            System.out.println("File đã được tải lên S3 với đường dẫn: " + s3FilePath);
            Path path = Paths.get(localFilePath);
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public String uploadFileToS3(File localFilePath, String fileName, String title) {
//        // Đường dẫn trên S3 (thư mục "video" và tên file)
//        String s3FilePath = "video/" + fileName;
//
//        // Tải file lên S3
//        PutObjectResult putObjectResult = amazonS3.putObject(new PutObjectRequest(bucketName, s3FilePath, localFilePath));
//        if (putObjectResult.getETag() != null) {
//            URL s3Url = amazonS3.getUrl(bucketName, s3FilePath);
//            System.out.printf("File đã được tải lên S3 với đường dẫn: %s, ID: %s, S3 URL: %s%n"
//                    , s3FilePath, putObjectResult.getETag(), s3Url.toExternalForm());
//        }


        // Đường dẫn trên S3 (thư mục "video" và tên file)
        String s3FilePath = title.concat("/") + fileName;
        // Tải file lên S3
        PutObjectResult putObjectResult = amazonS3.putObject(new PutObjectRequest(bucketName, s3FilePath, localFilePath));
        if (putObjectResult.getETag() != null) {
            URL s3Url = amazonS3.getUrl(bucketName, s3FilePath);
            System.out.printf("File đã được tải lên S3 với đường dẫn \n: %s, ID: %s, S3 URL: %s%n"
                    , s3FilePath, putObjectResult.getETag(), s3Url.toExternalForm());
            return s3Url.toExternalForm();
        }
        return "";
    }

    public S3ObjectInputStream getFileFromS3(String s3FilePath) {
        // Tải file lên S3
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, s3FilePath));
        if (s3Object.getObjectMetadata() != null) {
            return s3Object.getObjectContent();
        }
        return null;
    }
}
