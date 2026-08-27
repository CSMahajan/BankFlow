package com.bankflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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
            String scanResultStatus
    ) {
    }
}