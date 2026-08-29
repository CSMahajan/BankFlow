import React from "react";
import { formatDateTime } from "../../utils/formatUtils";
import styles from "./KycDocumentDetailsStyles";

const KycDocumentDetails = ({
    document,
    extractionData,
    panData,
    aadhaarData,
    previewDocument,
    processing,
    downloadingId,
    onView,
    onDownload,
    onVerify,
    onReject,
    onRetryExtraction,
    onRetryMalwareScan,
    onHidePreview,
    getStatusColor,
    getVerificationMessage
}) => {
    const statusStyle = getStatusColor(document.status);
    const verificationMessage = getVerificationMessage();

    const extractionSuccessful =
        extractionData?.extractionStatus === "SUCCESS";

    return (
        <div style={styles.detailsContainer}>

            <div style={styles.expandedHeader}>
                <div>
                    <h3 style={styles.customerHeading}>
                        👤 {document.customerName}
                    </h3>

                    <p style={styles.subtitle}>
                        Review submitted KYC document and verify customer identity.
                    </p>

                    {document.status === "REJECTED" &&
                        document.rejectionReason && (
                            <div style={styles.rejectionInfo}>
                                <strong>Rejection Reason:</strong>

                                <span>
                                    {document.rejectionReason}
                                </span>
                            </div>
                        )}
                </div>

                <span
                    style={{
                        ...styles.expandedStatusBadge,
                        backgroundColor: statusStyle.background,
                        color: statusStyle.color
                    }}
                >
                    {document.status}
                </span>
            </div>

            <div style={styles.reviewLayout}>

                <div style={styles.infoSection}>
                    <h4 style={styles.sectionTitle}>
                        Customer & Document Details
                    </h4>

                    <div style={styles.detailsGrid}>
                        <InfoCard
                            label="👤 Customer ID"
                            value={document.userId}
                        />

                        <InfoCard
                            label="👤 Customer Name"
                            value={document.customerName}
                        />

                        <InfoCard
                            label="✉️ Email"
                            value={document.email}
                        />

                        <InfoCard
                            label="📄 Document Type"
                            value={document.documentType}
                        />

                        <InfoCard
                            label="📎 File Name"
                            value={document.originalFileName}
                        />

                        <InfoCard
                            label="📅 Uploaded Date"
                            value={formatDateTime(document.uploadedAt)}
                        />
                    </div>
                </div>

                <div style={styles.infoSection}>
                    <h4 style={styles.sectionTitle}>
                        🔍 OCR Extraction
                    </h4>

                    {extractionData ? (
                        <>
                            <p>
                                Status:{" "}
                                <strong>
                                    {extractionData.extractionStatus}
                                </strong>
                            </p>

                            {extractionData.extractionStatus === "FAILED" && (
                                <button
                                    style={styles.retryButton}
                                    disabled={processing}
                                    onClick={() =>
                                        onRetryExtraction(document.id)
                                    }
                                >
                                    {processing
                                        ? "Retrying..."
                                        : "🔄 Retry Extraction"}
                                </button>
                            )}
                        </>
                    ) : (
                        <p style={styles.mutedText}>
                            Loading extraction data...
                        </p>
                    )}
                </div>

                <div style={styles.infoSection}>
                    <h4 style={styles.sectionTitle}>
                        🛡️ Malware Scan
                    </h4>

                    {extractionData ? (
                        <>
                            <p>
                                Status:{" "}
                                <strong>
                                    {extractionData.malwareScanStatus || "UNKNOWN"}
                                </strong>
                            </p>

                            <p>
                                Attempts:{" "}
                                <strong>
                                    {extractionData.malwareScanAttempt ?? 0}
                                </strong>
                            </p>

                            {extractionData.malwareScanStatus === "FAILED" && (
                                <button
                                    style={styles.retryButton}
                                    disabled={processing}
                                    onClick={() =>
                                        onRetryMalwareScan(document.id)
                                    }
                                >
                                    {processing
                                        ? "Retrying..."
                                        : "🔄 Retry Malware Scan"}
                                </button>
                            )}
                        </>
                    ) : (
                        <p style={styles.mutedText}>
                            Loading malware scan data...
                        </p>
                    )}
                </div>

                {panData && (
                    <div style={styles.infoSection}>
                        <h4 style={styles.sectionTitle}>
                            🪪 Extracted PAN Details
                        </h4>

                        <div style={styles.detailsGrid}>
                            <InfoCard
                                label="PAN Number"
                                value={panData.panNumber}
                                valueStyle={styles.documentNumberValue}
                            />

                            <InfoCard
                                label="Name"
                                value={panData.fullName}
                            />

                            <InfoCard
                                label="Father Name"
                                value={panData.fatherName}
                            />

                            <InfoCard
                                label="DOB"
                                value={panData.dateOfBirth}
                            />
                        </div>
                    </div>
                )}

                {aadhaarData && (
                    <div style={styles.infoSection}>
                        <h4 style={styles.sectionTitle}>
                            🆔 Extracted Aadhaar Details
                        </h4>

                        <div style={styles.detailsGrid}>
                            <InfoCard
                                label="Aadhaar Number"
                                value={aadhaarData.aadhaarNumber}
                                valueStyle={styles.documentNumberValue}
                            />

                            <InfoCard
                                label="Name"
                                value={aadhaarData.fullName}
                            />

                            <InfoCard
                                label="DOB"
                                value={aadhaarData.dateOfBirth}
                            />

                            <InfoCard
                                label="Gender"
                                value={aadhaarData.gender}
                            />

                            <InfoCard
                                label="Mobile"
                                value={aadhaarData.mobileNumber}
                            />

                            <InfoCard
                                label="Address"
                                value={aadhaarData.address}
                            />
                        </div>
                    </div>
                )}

                <div style={styles.previewSection}>

                    <div style={styles.documentHeader}>
                        <div>
                            <strong>
                                📄 {document.documentType} Document
                            </strong>

                            <small style={styles.documentName}>
                                {document.originalFileName}
                            </small>
                        </div>

                        <div style={styles.previewActions}>
                            <button
                                style={styles.viewButton}
                                onClick={() => {
                                    if (
                                        previewDocument?.id === document.id
                                    ) {
                                        onHidePreview();
                                    } else {
                                        onView(document);
                                    }
                                }}
                            >
                                {previewDocument?.id === document.id
                                    ? "🙈 Hide"
                                    : "👁 View"}
                            </button>

                            <button
                                style={styles.downloadButton}
                                disabled={downloadingId === document.id}
                                onClick={() =>
                                    onDownload(document)
                                }
                            >
                                {downloadingId === document.id
                                    ? "Downloading..."
                                    : "⬇ Download"}
                            </button>
                        </div>
                    </div>

                    {previewDocument?.id === document.id ? (
                        <div style={styles.previewContainer}>
                            {previewDocument.type.includes("pdf") ? (
                                <iframe
                                    src={previewDocument.url}
                                    title="KYC Preview"
                                    style={styles.previewFrame}
                                />
                            ) : (
                                <img
                                    src={previewDocument.url}
                                    alt="KYC"
                                    style={styles.previewImage}
                                />
                            )}
                        </div>
                    ) : (
                        <div style={styles.noPreview}>
                            Click View to open document preview
                        </div>
                    )}

                    {document.status === "PENDING" && (
                        <div style={styles.documentActionPanel}>

                            <p style={styles.actionText}>
                                Review document and verify customer identity.
                            </p>

                            <div style={styles.detailActions}>

                                <button
                                    style={styles.rejectButton}
                                    disabled={processing}
                                    onClick={() =>
                                        onReject(document.id)
                                    }
                                >
                                    Reject
                                </button>

                                <div style={styles.verifyContainer}>
                                    <button
                                        style={{
                                            ...styles.verifyButton,
                                            opacity: extractionSuccessful
                                                ? 1
                                                : 0.5,
                                            cursor: extractionSuccessful
                                                ? "pointer"
                                                : "not-allowed"
                                        }}
                                        disabled={
                                            processing ||
                                            !extractionSuccessful
                                        }
                                        title={verificationMessage || ""}
                                        onClick={() =>
                                            onVerify(document.id)
                                        }
                                    >
                                        Verify
                                    </button>

                                    {verificationMessage && (
                                        <span style={styles.verifyHint}>
                                            {verificationMessage}
                                        </span>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};


const InfoCard = ({
    label,
    value,
    valueStyle
}) => {
    return (
        <div style={styles.infoCard}>
            <span style={styles.infoLabel}>
                {label}
            </span>

            <p
                style={{
                    ...styles.infoValue,
                    ...valueStyle
                }}
            >
                {value}
            </p>
        </div>
    );
};

export default KycDocumentDetails;