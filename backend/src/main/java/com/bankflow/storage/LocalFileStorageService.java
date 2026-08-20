package com.bankflow.storage;

import com.bankflow.dto.StoredFileMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(
        name="app.storage.type",
        havingValue="local",
        matchIfMissing=true
)
public class LocalFileStorageService implements FileStorageService {
    private final Path rootLocation;

    public LocalFileStorageService(@Value("${app.storage.location}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        createDirectory();
    }

    private void createDirectory() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public StoredFileMetadata store(MultipartFile file, Long userId) {
        try {
            String userFolder = "user-" + userId;
            Path userDirectory = rootLocation.resolve(userFolder);
            Files.createDirectories(userDirectory);
            String extension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                extension =
                        originalFilename.substring(
                                originalFilename.lastIndexOf(".")
                        );
            }
            String storedFileName = UUID.randomUUID() + extension;
            Path destination = userDirectory.resolve(storedFileName).normalize();
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully: {}", destination);

            return new StoredFileMetadata(
                    userFolder + "/" + storedFileName,
                    "LOCAL",
                    null,
                    null,
                    null,
                    null
            );
        } catch (IOException e) {
            throw new RuntimeException("File storage failed", e);
        }
    }

    @Override
    public void delete(String filePath) {

        try {

            Path file =
                    rootLocation
                            .resolve(filePath)
                            .normalize();


            if (!file.startsWith(rootLocation)) {
                throw new RuntimeException(
                        "Invalid file path"
                );
            }


            Files.deleteIfExists(file);

        } catch (IOException e) {

            log.error(
                    "Failed deleting file {}",
                    filePath,
                    e
            );
        }
    }

    @Override
    public Resource load(String filePath) {
        try {

            Path file = rootLocation
                    .resolve(filePath)
                    .normalize();

            if (!file.startsWith(rootLocation)) {
                throw new RuntimeException("Invalid file path");
            }

            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("File not found");

        } catch (Exception e) {
            throw new RuntimeException("Unable to read file", e);
        }
    }
}