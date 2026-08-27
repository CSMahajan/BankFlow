package com.bankflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GuardDutyScanEvent(
        String id,

        @JsonProperty("detail-type")
        String detailType,

        String source,
        String region,
        Detail detail
) {

    public record Detail(
            String schemaVersion,
            String scanStatus,
            String resourceType,
            S3ObjectDetails s3ObjectDetails,
            ScanResultDetails scanResultDetails
    ) {
    }

    public record S3ObjectDetails(
            String bucketName,
            String objectKey,
            String eTag,
            String versionId,
            Boolean s3Throttled
    ) {
    }

    public record ScanResultDetails(
            String scanResultStatus,
            List<Threat> threats,
            List<String> statusReasons
    ) {
    }

    public record Threat(
            String name,
            String source,
            List<ItemDetail> itemDetails
    ) {
    }

    public record ItemDetail(
            String hash,
            String itemPath
    ) {
    }
}