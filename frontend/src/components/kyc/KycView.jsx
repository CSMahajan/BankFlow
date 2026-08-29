import React, { useEffect, useState } from "react";
import {
    fetchMyKycStatus, fetchMyKycDocuments,
    uploadKycDocument, viewMyKycDocument,
    fetchMyPanData, fetchMyAadhaarData
} from "../../api/bankService";
import KycStatusCard from "./KycStatusCard";
import KycVerifiedIdentity from "./KycVerifiedIdentity";
import KycDocuments from "./KycDocuments";
import KycHeader from "./KycHeader";
import KycSuccessBanner from "./KycSuccessBanner";
import toast from "react-hot-toast";


const KycView = () => {

    const [kycStatus, setKycStatus] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [uploadingType, setUploadingType] = useState(null);
    const [previewDocument, setPreviewDocument] = useState(null);
    const [selectedFiles, setSelectedFiles] = useState({});
    const [kycDetails, setKycDetails] = useState({
        pan: null,
        aadhaar: null
    });

    const [showPan, setShowPan] = useState(false);
    const [showAadhaar, setShowAadhaar] = useState(false);

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

            toast.error(
                err.response?.data?.message ||
                "Unable to load KYC details"
            );

        } finally {

            setLoading(false);

        }
    };

    const handleViewPan = async () => {

        try {

            const panDocument =
                documents.find(
                    d => d.documentType === "PAN"
                );


            const data =
                await fetchMyPanData(panDocument.id);


            setKycDetails(prev => ({
                ...prev,
                pan: data.panNumber
            }));

            setShowPan(true);


        } catch (err) {

            toast.error("Unable to fetch PAN details");

        }
    };


    const handleViewAadhaar = async () => {

        try {

            const aadhaarDocument =
                documents.find(
                    d => d.documentType === "AADHAAR"
                );


            const data =
                await fetchMyAadhaarData(aadhaarDocument.id);


            setKycDetails(prev => ({
                ...prev,
                aadhaar: data.aadhaarNumber
            }));

            setShowAadhaar(true);


        } catch (err) {

            toast.error("Unable to fetch Aadhaar details");

        }

    };

    const maskPan = (pan) => {
        if (!pan) return "";

        return (
            "*".repeat(pan.length - 4) +
            pan.slice(-4)
        );
    };


    const maskAadhaar = (aadhaar) => {
        if (!aadhaar) return "";

        const clean = aadhaar.replace(/\s/g, "");

        return (
            "*".repeat(clean.length - 4) +
            clean.slice(-4)
        );
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
            <KycHeader styles={styles} />
            <KycStatusCard
                status={kycStatus?.overallStatus}
            />

            {
                kycStatus?.overallStatus === "VERIFIED" && (
                    <KycSuccessBanner styles={styles} />
                )
            }

            {
                kycStatus?.overallStatus === "VERIFIED" && (

                    <KycVerifiedIdentity
                        kycDetails={kycDetails}
                        showPan={showPan}
                        showAadhaar={showAadhaar}
                        onViewPan={(show) => {
                            if (show) {
                                handleViewPan();
                            }
                            else {
                                setShowPan(false);
                            }
                        }}
                        onViewAadhaar={(show) => {
                            if (show) {
                                handleViewAadhaar();
                            }
                            else {
                                setShowAadhaar(false);
                            }
                        }}
                        maskPan={maskPan}
                        maskAadhaar={maskAadhaar}
                    />

                )
            }

            <KycDocuments
                documents={documents}
                uploadingType={uploadingType}
                onUpload={handleUpload}
                onView={handleViewDocument}
                previewDocument={previewDocument}
                onHide={handleHideDocument}
                selectedFiles={selectedFiles}
                getStatusColor={getStatusColor}
                styles={styles}
            />
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
    },

    smallButton: {
        background: "#0d6360",
        color: "#fff",
        border: "none",
        padding: "6px 12px",
        borderRadius: "6px",
        cursor: "pointer",
        fontSize: "12px"
    }
};

export default KycView;