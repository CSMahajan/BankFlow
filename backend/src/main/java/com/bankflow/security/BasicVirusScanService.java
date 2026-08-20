package com.bankflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class BasicVirusScanService implements VirusScanService {

    @Override
    public VirusScanResult scan(MultipartFile file) {

        log.info(
                "Security scan completed for file: {}",
                file.getOriginalFilename()
        );

        // Real antivirus provider can replace this later
        return VirusScanResult.success();
    }
}