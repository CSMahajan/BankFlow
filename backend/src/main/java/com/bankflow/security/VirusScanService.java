package com.bankflow.security;

import org.springframework.web.multipart.MultipartFile;

public interface VirusScanService {

    VirusScanResult scan(MultipartFile file);

}