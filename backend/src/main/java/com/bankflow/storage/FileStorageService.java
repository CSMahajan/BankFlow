package com.bankflow.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String store(MultipartFile file, Long userId);

    void delete(String filePath);

    Resource load(String filePath);
}