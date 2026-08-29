import React, { useEffect, useState } from "react";
import {
    viewAdminKycDocument,
    downloadAdminKycDocument,
    verifyKycDocument,
    rejectKycDocument,
    fetchAdminKycExtraction,
    fetchAdminPanData,
    fetchAdminAadhaarData,
    retryAdminKycExtraction,
    retryAdminKycMalwareScan
} from "../../api/bankService";
import useKycData from "./useKycData";
import { getStatusColor } from "./kycUtils";
import KycSearchToolbar from "./KycSearchToolbar";
import KycPagination from "./KycPagination";
import KycSummaryCards from "./KycSummaryCards";
import KycDocumentDetails from "./KycDocumentDetails";
import KycRejectModal from "./KycRejectModal";
import KycManagementHeader from "./KycManagementHeader";
import KycDocumentsTable from "./KycDocumentsTable";
import toast from "react-hot-toast";


const KycManagementView = ({
    refreshDashboard
}) => {

    const {
        documents, summary,
        loading, initialLoading,
        page, setPage, totalPages,
        search, setSearch,
        status, setStatus, loadData
    } = useKycData();

    const [previewDocument, setPreviewDocument] = useState(null);
    const [selectedDocument, setSelectedDocument] = useState(null);
    const [rejectDocumentId, setRejectDocumentId] = useState(null);
    const [rejectionReason, setRejectionReason] = useState("");
    const [processing, setProcessing] = useState(false);
    const [extractionData, setExtractionData] = useState(null);
    const [panData, setPanData] = useState(null);
    const [aadhaarData, setAadhaarData] = useState(null);
    const [downloadingId, setDownloadingId] = useState(null);

    const handleViewDocument = async (doc) => {

        try {
            const blob = await viewAdminKycDocument(doc.id);
            const url = window.URL.createObjectURL(blob);
            if (previewDocument?.url) {
                window.URL.revokeObjectURL(previewDocument.url);
            }
            setPreviewDocument({
                id: doc.id,
                name: doc.originalFileName,
                type: blob.type,
                url
            });
        } catch (err) {
            console.error(err);
            toast.error("Unable to open document");
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

    const handleDownloadDocument = async (doc) => {
        try {
            setDownloadingId(doc.id);

            const blob = await downloadAdminKycDocument(doc.id);

            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = url;
            link.download = doc.originalFileName;

            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);

        } catch (err) {
            toast.error("Unable to download document");
        } finally {
            setDownloadingId(null);
        }
    };


    const handleVerify = async (documentId) => {
        try {
            setProcessing(true);
            await verifyKycDocument(documentId);
            toast.success("Document verified successfully");
            await loadData();
            if (refreshDashboard) {
                await refreshDashboard();
            }
        } catch (err) {
            console.error(err);
            toast.error(
                err.response?.data?.message ||
                "Unable to verify document"
            );
        } finally {
            setProcessing(false);
        }
    };

    const handleReject = async () => {
        if (!rejectionReason.trim()) {
            toast.error(
                "Rejection reason is required"
            );
            return;
        }
        try {
            setProcessing(true);
            await rejectKycDocument(
                rejectDocumentId,
                rejectionReason
            );
            toast.success(
                "Document rejected"
            );
            setRejectDocumentId(null);
            setRejectionReason("");
            await loadData();
            if (refreshDashboard) {
                await refreshDashboard();
            }
        } catch (err) {
            console.error(err);
            toast.error(
                err.response?.data?.message ||
                "Unable to reject document"
            );
        } finally {
            setProcessing(false);
        }
    };

    useEffect(() => {
        setSelectedDocument(null);
        if (previewDocument?.url) {
            window.URL.revokeObjectURL(previewDocument.url);
        }
        setPreviewDocument(null);
    }, [page, status, search]);

    const loadKycReviewData = async (doc) => {

        try {

            const extraction = await fetchAdminKycExtraction(
                doc.id
            );

            setExtractionData(extraction);


            if (doc.documentType === "PAN") {

                const pan =
                    await fetchAdminPanData(doc.id);

                setPanData(pan);
                setAadhaarData(null);

            }


            if (doc.documentType === "AADHAAR") {

                const aadhaar =
                    await fetchAdminAadhaarData(doc.id);

                setAadhaarData(aadhaar);
                setPanData(null);

            }


        } catch (err) {

            console.error(err);

            toast.error(
                "Unable to load extracted KYC data"
            );

        }

    };

    const getVerificationMessage = () => {

        switch (extractionData?.extractionStatus) {

            case "PENDING":
                return "⏳ OCR extraction is still processing";

            case "FAILED":
                return "⚠ OCR extraction failed. Retry extraction first";

            case "SUCCESS":
                return null;

            default:
                return "⏳ OCR extraction status unavailable";
        }

    };

    const handleRetryExtraction = async (documentId) => {

        try {

            setProcessing(true);

            await retryAdminKycExtraction(documentId);

            toast.success(
                "Extraction retry initiated"
            );

            setExtractionData(prev => ({
                ...(prev || {}),
                extractionStatus: "PENDING",
                failureReason: null
            }));

        } catch (err) {

            console.error(err);

            toast.error(
                err.response?.data?.message ||
                "Unable to retry extraction"
            );

        } finally {

            setProcessing(false);

        }
    };

    const handleRetryMalwareScan = async (documentId) => {

        try {
            setProcessing(true);
            await retryAdminKycMalwareScan(documentId);
            toast.success("Malware scan retry initiated");
            setExtractionData(prev => ({
                ...(prev || {}),
                malwareScanStatus: "PENDING"
            }));
        } catch (err) {
            console.error(err);
            toast.error(err.response?.data?.message || "Unable to retry malware scan");
        } finally {
            setProcessing(false);
        }
    };

    useEffect(() => {
        if (!selectedDocument) {
            return;
        }
        const isProcessing =
            extractionData?.extractionStatus === "PENDING" || extractionData?.malwareScanStatus === "PENDING";
        if (!isProcessing) {
            return;
        }
        const interval = setInterval(async () => {
            try {
                const extraction = await fetchAdminKycExtraction(selectedDocument.id);
                setExtractionData(extraction);
                const extractionFinished = extraction.extractionStatus !== "PENDING";
                const malwareFinished = extraction.malwareScanStatus !== "PENDING";
                if (extractionFinished && malwareFinished) {
                    clearInterval(interval);
                    await loadKycReviewData(
                        selectedDocument
                    );
                }
            } catch (err) {
                console.error(err);
            }
        }, 5000);
        return () => clearInterval(interval);
    }, [
        selectedDocument,
        extractionData?.extractionStatus,
        extractionData?.malwareScanStatus
    ]);

    useEffect(() => {
        return () => {
            if (previewDocument?.url) {
                window.URL.revokeObjectURL(previewDocument.url);
            }
        };
    }, [previewDocument]);

    if (initialLoading) {
        return (
            <div>
                Loading KYC data...
            </div>
        )
    }

    return (

        <div style={styles.container}>
            <KycManagementHeader
                onRefresh={loadData}
                loading={loading}
            />
            {
                summary && (
                    <KycSummaryCards summary={summary} />
                )
            }
            <div style={{ position: "relative" }}>

                <KycSearchToolbar
                    search={search}
                    setSearch={setSearch}
                    status={status}
                    setStatus={setStatus}
                />

                {
                    loading && (
                        <span style={styles.loadingText}>
                            Loading...
                        </span>
                    )
                }

            </div>

            <KycDocumentsTable
                documents={documents}
                selectedDocument={selectedDocument}
                loading={loading}
                onSelect={(doc) => {
                    if (selectedDocument?.id === doc.id) {
                        setSelectedDocument(null);
                        setExtractionData(null);
                        setPanData(null);
                        setAadhaarData(null);
                        handleHideDocument();
                        return;
                    }

                    setSelectedDocument(doc);
                    setExtractionData(null);
                    setPanData(null);
                    setAadhaarData(null);
                    loadKycReviewData(doc);
                }}
                renderDetails={(doc) => (
                    <KycDocumentDetails
                        document={doc}
                        extractionData={extractionData}
                        panData={panData}
                        aadhaarData={aadhaarData}
                        previewDocument={previewDocument}
                        processing={processing}
                        downloadingId={downloadingId}
                        onView={handleViewDocument}
                        onDownload={handleDownloadDocument}
                        onVerify={handleVerify}
                        onReject={setRejectDocumentId}
                        onRetryExtraction={handleRetryExtraction}
                        onRetryMalwareScan={handleRetryMalwareScan}
                        onHidePreview={handleHideDocument}
                        getStatusColor={getStatusColor}
                        getVerificationMessage={getVerificationMessage}
                    />
                )}
            />

            <KycPagination
                totalPages={totalPages}
                page={page}
                setPage={setPage}
            />

            {
                rejectDocumentId && (
                    <KycRejectModal
                        rejectionReason={rejectionReason}
                        setRejectionReason={setRejectionReason}
                        onCancel={() => {
                            setRejectDocumentId(null);
                            setRejectionReason("");
                        }}
                        onReject={handleReject}
                    />
                )
            }
        </div>

    );

};

const styles = {
    container: {
        display: "flex",
        flexDirection: "column",
        gap: "20px",
        width: "100%"
    },

    loadingText: {
        position: "absolute",
        right: "0",
        top: "-18px",
        fontSize: "12px",
        color: "#64748b",
        fontWeight: "600"
    }
};


export default KycManagementView;