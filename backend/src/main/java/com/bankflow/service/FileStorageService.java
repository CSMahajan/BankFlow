package com.bankflow.service;

import com.bankflow.dto.StoredFileMetadata;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFileMetadata store(MultipartFile file, Long userId);

    void delete(String filePath);

    Resource load(String filePath);
}