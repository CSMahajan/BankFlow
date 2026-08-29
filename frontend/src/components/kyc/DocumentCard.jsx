import React from "react";

const DocumentCard = ({
    title,
    icon,
    type,
    document,
    uploadingType,
    onUpload,
    onView,
    previewDocument,
    onHide,
    selectedFile,
    getStatusColor,
    styles
}) => {

    const canUpload = !document || document.status === "REJECTED";

    return (
        <div style={styles.card}>

            <div style={styles.cardHeader}>

                <span style={styles.documentIcon}>
                    {icon}
                </span>

                <div>
                    <h3 style={styles.cardTitle}>
                        {title}
                    </h3>

                    <p style={styles.cardSubtitle}>
                        Upload clear {title} image or PDF
                    </p>
                </div>

            </div>

            <div style={styles.statusRow}>

                <span>
                    Status
                </span>

                <strong
                    style={{
                        padding: "4px 10px",
                        borderRadius: "12px",
                        backgroundColor:
                            getStatusColor(
                                document?.status
                            ).background,
                        color:
                            getStatusColor(
                                document?.status
                            ).color,
                        fontSize: "12px"
                    }}
                >
                    {
                        document?.status ||
                        "NOT_UPLOADED"
                    }
                </strong>

            </div>

            {
                !document && (
                    <p style={styles.infoText}>
                        Please upload this document to complete KYC.
                    </p>
                )
            }

            {
                selectedFile && (
                    <div style={styles.selectedFile}>

                        📄 {selectedFile.name}

                        <br />

                        {
                            (selectedFile.size / 1024 / 1024)
                                .toFixed(2)
                        }
                        MB

                    </div>
                )
            }

            {
                document?.status === "PENDING" && (
                    <p style={styles.infoText}>
                        Your document is under review.
                    </p>
                )
            }

            {
                document?.status === "VERIFIED" && (
                    <p style={styles.infoText}>
                        Document verified successfully.
                    </p>
                )
            }

            {
                document?.uploadedAt && (
                    <p style={styles.infoText}>
                        Uploaded On:
                        {" "}
                        {
                            new Date(
                                document.uploadedAt
                            ).toLocaleDateString()
                        }
                    </p>
                )
            }

            {
                document?.originalFileName && (
                    <p style={styles.infoText}>
                        Uploaded File:
                        {" "}
                        {document.originalFileName}
                    </p>
                )
            }

            {
                document?.id && (
                    <button
                        style={styles.viewButton}
                        onClick={() => {

                            if (!document?.id) {
                                return;
                            }

                            if (
                                previewDocument?.id === document.id
                            ) {
                                onHide();
                            }
                            else {
                                onView(document.id);
                            }

                        }}
                    >
                        {
                            previewDocument?.id === document?.id
                                ?
                                "🙈 Hide Document"
                                :
                                "👁 View Document"
                        }
                    </button>
                )
            }

            {
                previewDocument &&
                previewDocument.id === document?.id && (

                    <div style={styles.previewContainer}>

                        <p style={styles.previewTitle}>
                            Preview: {document.originalFileName}
                        </p>

                        {
                            previewDocument.type?.includes("pdf")
                                ?
                                (
                                    <iframe
                                        src={previewDocument.url}
                                        title={title}
                                        style={styles.previewFrame}
                                    />
                                )
                                :
                                (
                                    <img
                                        src={previewDocument.url}
                                        alt={title}
                                        style={styles.previewImage}
                                    />
                                )
                        }

                    </div>

                )
            }

            {
                document?.rejectionReason && (
                    <div style={styles.rejectBox}>

                        <strong>
                            ❌ Document Rejected
                        </strong>

                        <p style={{ margin: "6px 0 0" }}>
                            Reason: {document.rejectionReason}
                        </p>

                    </div>
                )
            }

            {
                canUpload && (
                    <p style={styles.uploadHint}>
                        Accepted formats: PDF, JPG, PNG
                        <br />
                        Maximum size: 5 MB
                    </p>
                )
            }

            {
                canUpload && (
                    <label style={styles.uploadButton}>

                        {
                            uploadingType === type
                                ?
                                "Uploading..."
                                :
                                "Upload Document"
                        }

                        <input
                            type="file"
                            hidden
                            accept=".pdf,.jpg,.jpeg,.png"
                            disabled={
                                uploadingType !== null
                            }
                            onChange={(e) =>
                                onUpload(
                                    e,
                                    type
                                )
                            }
                        />

                    </label>
                )
            }

        </div>
    );
};

export default DocumentCard;