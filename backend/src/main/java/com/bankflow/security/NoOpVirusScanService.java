package com.bankflow.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@Slf4j
public class NoOpVirusScanService implements VirusScanService {


    @Override
    public VirusScanResult scan(MultipartFile file) {

        log.info(
                "Virus scan skipped for file: {}",
                file.getOriginalFilename()
        );

        return VirusScanResult.success();
    }
}