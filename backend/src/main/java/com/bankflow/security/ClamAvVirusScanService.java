package com.bankflow.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClamAvVirusScanService implements VirusScanService {

    @Value("${app.security.virus-scan-enabled:false}")
    private boolean isVirusScanEnabled;

    private final ClamAvClient clamAvClient;

    @Override
    public VirusScanResult scan(MultipartFile file) {
        if (!isVirusScanEnabled) {
            log.info("Virus scan disabled. Skipping file: {}", file.getOriginalFilename());
            return VirusScanResult.success();
        }
        try {
            String scanResponse = clamAvClient.scan(file.getInputStream());
            log.info("ClamAV response for {} : {}", file.getOriginalFilename(), scanResponse);
            if (scanResponse.contains("FOUND")) {
                return VirusScanResult.failure(scanResponse);
            }
            return VirusScanResult.success();
        } catch (Exception e) {
            log.error("Virus scan failed for file: {}", file.getOriginalFilename(), e);
            return VirusScanResult.failure("Unable to complete virus scan");
        }
    }
}