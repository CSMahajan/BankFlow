package com.bankflow.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

@Service
@Slf4j
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
    public String store(MultipartFile file, Long userId) {
        try {
            String userFolder = "user-" + userId;
            Path userDirectory = rootLocation.resolve(userFolder);
            Files.createDirectories(userDirectory);
            String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destination = userDirectory.resolve(storedFileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored successfully: {}", destination);
            // return relative path only
            return userFolder + "/" + storedFileName;
        } catch (IOException e) {
            throw new RuntimeException("File storage failed", e);
        }
    }

    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(rootLocation.resolve(filePath));
        } catch (IOException e) {
            log.error("Failed deleting file {}", filePath);
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