package com.bankflow.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.storage.type",
        havingValue = "s3"
)
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${app.aws.bucket}")
    private String bucketName;


    @Override
    public String store(MultipartFile file, Long userId) {

        try {

            String key = "user-" + userId
                    + "/"
                    + UUID.randomUUID()
                    + "_"
                    + file.getOriginalFilename();


            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();


            s3Client.putObject(
                    request,
                    software.amazon.awssdk.core.sync.RequestBody
                            .fromInputStream(
                                    file.getInputStream(),
                                    file.getSize()
                            )
            );


            log.info("File uploaded to S3 successfully: {}", key);

            return key;

        } catch (IOException e) {
            throw new RuntimeException(
                    "S3 file upload failed",
                    e
            );
        }
    }


    @Override
    public void delete(String filePath) {

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filePath)
                        .build();

        s3Client.deleteObject(request);

        log.info("File deleted from S3: {}", filePath);
    }


    @Override
    public Resource load(String filePath) {

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filePath)
                        .build();


        ResponseInputStream<GetObjectResponse> response =
                s3Client.getObject(request);


        return new InputStreamResource(response);
    }
}