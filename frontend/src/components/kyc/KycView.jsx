import React, { useEffect, useState } from "react";
import {
    fetchMyKycStatus, fetchMyKycDocuments, uploadKycDocument, viewMyKycDocument
} from "../../api/bankService";
import toast from "react-hot-toast";


const KycView = () => {

    const [kycStatus, setKycStatus] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [uploadingType, setUploadingType] = useState(null);
    const [previewDocument, setPreviewDocument] = useState(null);
    const [selectedFiles, setSelectedFiles] = useState({});

    useEffect(() => {
        loadKyc();
    }, []);

    useEffect(() => {
        return () => {
            if (previewDocument?.url) {
                window.URL.revokeObjectURL(previewDocument.url);
            }
        };
    }, [previewDocument]);


    const loadKyc = async () => {
        try {
            setLoading(true);
            const [
                status,
                docs
            ] = await Promise.all([
                fetchMyKycStatus(),
                fetchMyKycDocuments()
            ]);

            setKycStatus(status);
            setDocuments(docs || []);
        } catch (err) {
            console.error(err);
            toast.error(err.response?.data?.message || "Unable to load KYC details");
        } finally {
            setLoading(false);
        }
    };

    const handleUpload = async (
        event,
        documentType
    ) => {
        const file = event.target.files[0];

        if (!file) {
            return;
        }


        const allowedTypes = [
            "application/pdf",
            "image/png",
            "image/jpeg"
        ];


        if (!allowedTypes.includes(file.type)) {

            toast.error(
                "Only PDF, JPG and PNG files are allowed"
            );

            event.target.value = "";
            return;
        }


        if (file.size > 5 * 1024 * 1024) {

            toast.error(
                "File size cannot exceed 5 MB"
            );

            event.target.value = "";
            return;
        }
        try {
            setUploadingType(documentType);
            setSelectedFiles(prev => ({
                ...prev,
                [documentType]: file
            }));
            await uploadKycDocument(file, documentType);
            toast.success(`${documentType} uploaded successfully`);
            await loadKyc();
            setSelectedFiles(prev => ({
                ...prev,
                [documentType]: null
            }));
        } catch (err) {
            console.error(err);
            toast.error(err.response?.data?.message || "Upload failed");
        } finally {
            setUploadingType(null);
            event.target.value = "";
        }
    };

    const handleViewDocument = async (
        documentId
    ) => {

        try {

            const blob = await viewMyKycDocument(
                documentId
            );

            const url = window.URL.createObjectURL(
                blob
            );

            if (previewDocument?.url) {
                window.URL.revokeObjectURL(
                    previewDocument.url
                );
            }

            setPreviewDocument({
                id: documentId,
                url,
                type: blob.type
            });


        } catch (err) {

            console.error(err);

            toast.error(
                "Unable to open document"
            );

        }

    };

    const handleHideDocument = () => {

        if (previewDocument?.url) {
            window.URL.revokeObjectURL(
                previewDocument.url
            );
        }

        setPreviewDocument(null);

    };

    if (loading) {
        return (
            <div style={styles.loading}>
                Loading KYC details...
            </div>
        );
    }

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <h2 style={styles.title}>
                    KYC Verification
                </h2>
                <p style={styles.subtitle}>
                    Verify your identity by uploading required documents.
                    This helps us keep your account secure.
                </p>
            </div>
            <div style={styles.statusCard}>
                <div>
                    <h3 style={styles.sectionTitle}>
                        🪪 Verification Status
                    </h3>
                    <p style={styles.statusDescription}>
                        Complete your KYC verification to access all banking services.
                    </p>
                </div>
                <div
                    style={{
                        ...styles.statusBadge,
                        backgroundColor:
                            getStatusColor(
                                kycStatus?.overallStatus
                            ).background,
                        color:
                            getStatusColor(
                                kycStatus?.overallStatus
                            ).color
                    }}
                >
                    <span>
                        ●
                    </span>

                    {kycStatus?.overallStatus || "PENDING"}
                </div>
            </div>

            {
                kycStatus?.overallStatus === "VERIFIED" && (

                    <div style={styles.successBanner}>
                        ✅ Your KYC verification is completed.
                        You can now access all banking services.
                    </div>

                )
            }

            <h3 style={styles.documentsTitle}>
                Required Documents
            </h3>


            <div style={styles.grid}>
                <DocumentCard
                    title="PAN Card"
                    icon="🪪"
                    type="PAN"
                    document={
                        documents.find(
                            d => d.documentType === "PAN"
                        )
                    }
                    uploadingType={uploadingType}
                    onUpload={handleUpload}
                    onView={handleViewDocument}
                    previewDocument={previewDocument}
                    onHide={handleHideDocument}
                    selectedFile={selectedFiles["PAN"]}
                />
                <DocumentCard
                    title="Aadhaar Card"
                    icon="🆔"
                    type="AADHAAR"
                    document={
                        documents.find(
                            d => d.documentType === "AADHAAR"
                        )
                    }
                    uploadingType={uploadingType}
                    onUpload={handleUpload}
                    onView={handleViewDocument}
                    previewDocument={previewDocument}
                    onHide={handleHideDocument}
                    selectedFile={selectedFiles["AADHAAR"]}
                />
            </div>
        </div>
    );
};

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
    selectedFile
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
                        <p>
                            {document.rejectionReason}
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

const getStatusColor = (status) => {

    switch (status) {

        case "VERIFIED":
            return {
                background: "#dcfce7",
                color: "#166534"
            };

        case "PENDING":
            return {
                background: "#fef3c7",
                color: "#92400e"
            };

        case "REJECTED":
            return {
                background: "#fee2e2",
                color: "#991b1b"
            };

        case "INCOMPLETE":
            return {
                background: "#fef3c7",
                color: "#92400e"
            };

        case "UNDER_REVIEW":
            return {
                background: "#dbeafe",
                color: "#1d4ed8"
            };

        default:
            return {
                background: "#e5e7eb",
                color: "#374151"
            };
    }

};

const styles = {

    container: {
        display: "flex",
        flexDirection: "column",
        gap: "20px"
    },

    header: {},

    title: {
        margin: 0,
        fontSize: "24px",
        fontFamily: "Georgia, serif"
    },

    subtitle: {
        color: "#6b7280",
        fontSize: "13px"
    },

    statusCard: {
        background: "#ffffff",
        padding: "24px",
        borderRadius: "14px",
        border: "1px solid #eef0ec",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        boxShadow: "0 4px 12px rgba(0,0,0,0.04)"
    },

    badge: {
        background: "#e6f2f1",
        color: "#0d6360",
        padding: "6px 12px",
        borderRadius: "20px",
        fontWeight: "700",
        fontSize: "13px"
    },

    grid: {
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit,minmax(280px,1fr))",
        gap: "20px"
    },

    card: {
        background: "#ffffff",
        padding: "24px",
        borderRadius: "14px",
        border: "1px solid #eef0ec",
        boxShadow: "0 4px 12px rgba(0,0,0,0.04)"
    },

    uploadButton: {
        display: "inline-flex",
        alignItems: "center",
        gap: "8px",
        background: "#0d6360",
        color: "#ffffff",
        padding: "11px 20px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "13px",
        marginTop: "18px"
    },

    rejectBox: {
        background: "#fef2f2",
        color: "#991b1b",
        padding: "12px",
        borderRadius: "8px",
        fontSize: "13px",
        marginTop: "14px"
    },

    sectionTitle: {
        margin: 0,
        fontSize: "18px",
        color: "#111827"
    },

    statusDescription: {
        marginTop: "6px",
        fontSize: "13px",
        color: "#6b7280"
    },

    documentsTitle: {
        margin: "5px 0",
        fontSize: "18px",
        color: "#111827"
    },

    cardHeader: {
        display: "flex",
        alignItems: "center",
        gap: "12px",
        marginBottom: "18px"
    },

    documentIcon: {
        fontSize: "26px"
    },

    cardTitle: {
        margin: 0,
        fontSize: "17px",
    },

    statusRow: {
        display: "flex",
        justifyContent: "space-between",
        padding: "12px 0",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px"
    },

    infoText: {
        fontSize: "13px",
        color: "#6b7280",
        marginTop: "12px"
    },

    loading: {
        background: "#ffffff",
        padding: "30px",
        borderRadius: "12px",
        border: "1px solid #eef0ec",
        color: "#6b7280",
        fontSize: "14px",
    },

    statusBadge: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        padding: "8px 16px",
        borderRadius: "20px",
        fontWeight: "700",
        fontSize: "13px",
    },

    cardSubtitle: {
        margin: "4px 0 0",
        fontSize: "12px",
        color: "#6b7280",
    },

    successBanner: {
        background: "#ecfdf5",
        color: "#166534",
        padding: "14px 18px",
        borderRadius: "10px",
        fontSize: "14px",
        fontWeight: "600",
        border: "1px solid #bbf7d0"
    },

    uploadHint: {
        fontSize: "12px",
        color: "#6b7280",
        marginTop: "15px"
    },

    viewButton: {
        marginTop: "14px",
        background: "#ffffff",
        color: "#0d6360",
        border: "1px solid #0d6360",
        padding: "9px 16px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "13px"
    },

    previewContainer: {
        marginTop: "20px",
        border: "1px solid #e5e7eb",
        borderRadius: "10px",
        overflow: "hidden"
    },

    previewFrame: {
        width: "100%",
        height: "450px",
        border: "none"
    },

    previewImage: {
        width: "100%",
        maxHeight: "450px",
        objectFit: "contain",
        display: "block"
    },

    previewTitle: {
        padding: "10px 14px",
        margin: 0,
        fontSize: "13px",
        fontWeight: "700",
        background: "#f9fafb"
    }
};

export default KycView;