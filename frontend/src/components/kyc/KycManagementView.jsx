import React, { useEffect, useState } from "react";
import {
    fetchAdminKycDocuments,
    fetchAdminKycSummary,
    viewAdminKycDocument,
    downloadAdminKycDocument,
    verifyKycDocument,
    rejectKycDocument,
    fetchAdminKycExtraction,
    fetchAdminPanData,
    fetchAdminAadhaarData,
    retryAdminKycExtraction
} from "../../api/bankService";
import { formatDateTime } from "../../utils/formatUtils";
import KycSearchToolbar from "./KycSearchToolbar";
import KycPagination from "./KycPagination";
import toast from "react-hot-toast";


const KycManagementView = ({
    refreshDashboard
}) => {

    const [documents, setDocuments] = useState([]);
    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [search, setSearch] = useState("");
    const [status, setStatus] = useState("ALL");
    const [previewDocument, setPreviewDocument] = useState(null);
    const [selectedDocument, setSelectedDocument] = useState(null);
    const [rejectDocumentId, setRejectDocumentId] = useState(null);
    const [rejectionReason, setRejectionReason] = useState("");
    const [processing, setProcessing] = useState(false);
    const [initialLoading, setInitialLoading] = useState(true);
    const [extractionData, setExtractionData] = useState(null);
    const [panData, setPanData] = useState(null);
    const [aadhaarData, setAadhaarData] = useState(null);
    const [downloadingId, setDownloadingId] = useState(null);

    const loadData = async () => {

        try {
            setLoading(true);

            const [docs, summaryData] = await Promise.all([
                fetchAdminKycDocuments({
                    page,
                    size: 10,
                    search,
                    status
                }),
                fetchAdminKycSummary()
            ]);

            setDocuments(docs.content);
            setTotalPages(docs.totalPages);
            setSummary(summaryData);

        } catch (err) {

            console.error(err);
            toast.error("Unable to load KYC data");

        }
        finally {
            setLoading(false);
            setInitialLoading(false);
        }
    };

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

            default:
                return {
                    background: "#e5e7eb",
                    color: "#374151"
                };

        }

    };

    useEffect(() => {

        setSelectedDocument(null);

        if (previewDocument?.url) {
            window.URL.revokeObjectURL(previewDocument.url);
        }

        setPreviewDocument(null);


        const timer = setTimeout(() => {
            loadData();
        }, 500);


        return () => clearTimeout(timer);

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

    useEffect(() => {

        if (
            !selectedDocument ||
            extractionData?.extractionStatus !== "PENDING"
        ) {
            return;
        }


        const interval = setInterval(async () => {

            try {

                const extraction =
                    await fetchAdminKycExtraction(
                        selectedDocument.id
                    );

                setExtractionData(extraction);


                if (
                    extraction.extractionStatus === "SUCCESS" ||
                    extraction.extractionStatus === "FAILED"
                ) {

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
        extractionData?.extractionStatus
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

    const KycSummaryCards = ({
        summary
    }) => {

        const cards = [
            {
                title: "Total Documents",
                icon: "📄",
                count: summary?.totalDocuments ?? 0,
                style: {
                    background: "#F8FAFC",
                    border: "1px solid #CBD5E1"
                }
            },
            {
                title: "Pending Review",
                icon: "⏳",
                count: summary?.pendingDocuments ?? 0,
                style: {
                    background: "#FEFCE8",
                    border: "1px solid #FDE68A"
                }
            },
            {
                title: "Verified",
                icon: "✅",
                count: summary?.verifiedDocuments ?? 0,
                style: {
                    background: "#ECFDF5",
                    border: "1px solid #A7F3D0"
                }
            },
            {
                title: "Rejected",
                icon: "❌",
                count: summary?.rejectedDocuments ?? 0,
                style: {
                    background: "#FEF2F2",
                    border: "1px solid #FECACA"
                }
            },
            {
                title: "Pending Customers",
                icon: "👤",
                count: summary?.pendingCustomers ?? 0,
                style: {
                    background: "#EFF6FF",
                    border: "1px solid #BFDBFE"
                }
            }
        ];


        return (
            <div style={styles.summaryGrid}>
                {
                    cards.map(card => (
                        <div
                            key={card.title}
                            style={{
                                ...styles.summaryCard,
                                ...card.style
                            }}
                        >

                            <div style={styles.summaryTitle}>
                                <span>
                                    {card.icon}
                                </span>

                                <span>
                                    {card.title}
                                </span>
                            </div>


                            <div style={styles.summaryValue}>
                                {card.count}
                            </div>

                        </div>
                    ))
                }
            </div>
        );
    };


    return (

        <div style={styles.container}>


            <div style={styles.headerRow}>

                <h2 style={styles.title}>
                    KYC Verification Management
                </h2>


                <button
                    style={styles.refreshButton}
                    onClick={loadData}
                >
                    🔄 Refresh
                </button>


            </div>


            {
                summary && (
                    <KycSummaryCards summary={summary} />
                )
            }
            <div style={styles.toolbarWrapper}>

                <KycSearchToolbar
                    search={search}
                    setSearch={setSearch}
                    status={status}
                    setStatus={setStatus}
                    setPage={setPage}
                />


                {
                    loading && (
                        <span style={styles.loadingText}>
                            Loading...
                        </span>
                    )
                }

            </div>

            <div
                style={{
                    ...styles.card,
                    opacity: loading ? 0.6 : 1,
                    transition: "opacity 0.2s ease"
                }}
            >


                <table style={styles.table}>

                    <thead>

                        <tr>

                            <th style={styles.th}>
                                Customer
                            </th>

                            <th style={styles.th}>
                                Document
                            </th>

                            <th style={styles.th}>
                                Type
                            </th>

                            <th style={styles.th}>
                                Status
                            </th>

                            <th style={styles.th}>
                                Uploaded
                            </th>

                        </tr>

                    </thead>


                    <tbody>

                        {
                            documents.map(doc => (
                                <React.Fragment key={doc.id}>

                                    <tr
                                        style={{
                                            cursor: "pointer",
                                            backgroundColor:
                                                selectedDocument?.id === doc.id
                                                    ? "#ecfeff"
                                                    : "#ffffff",
                                            borderLeft:
                                                selectedDocument?.id === doc.id
                                                    ? "3px solid #0d9488"
                                                    : "3px solid transparent",
                                            transition: "background-color 0.2s ease"
                                        }}
                                        onMouseEnter={(e) => {
                                            if (selectedDocument?.id !== doc.id) {
                                                e.currentTarget.style.backgroundColor = "#f8fafc";
                                            }
                                        }}
                                        onMouseLeave={(e) => {
                                            if (selectedDocument?.id !== doc.id) {
                                                e.currentTarget.style.backgroundColor = "#ffffff";
                                            }
                                        }}
                                        onClick={() => {
                                            if (selectedDocument?.id === doc.id) {
                                                setSelectedDocument(null);
                                                setExtractionData(null);
                                                setPanData(null);
                                                setAadhaarData(null);
                                                handleHideDocument();
                                            } else {

                                                setSelectedDocument(doc);

                                                setExtractionData(null);
                                                setPanData(null);
                                                setAadhaarData(null);

                                                loadKycReviewData(doc);
                                            }
                                        }}
                                    >

                                        <td style={styles.td}>
                                            <strong style={styles.customerName}>
                                                {doc.customerName}
                                            </strong>
                                            <br />
                                            <small>
                                                {doc.email}
                                            </small>
                                        </td>

                                        <td style={styles.td}>
                                            {doc.originalFileName}
                                        </td>

                                        <td style={styles.td}>
                                            {doc.documentType}
                                        </td>

                                        <td style={styles.td}>
                                            <span
                                                style={{
                                                    ...styles.statusBadge,
                                                    backgroundColor:
                                                        getStatusColor(doc.status).background,
                                                    color:
                                                        getStatusColor(doc.status).color
                                                }}
                                            >
                                                {doc.status}
                                            </span>
                                        </td>

                                        <td style={styles.td}>
                                            {formatDateTime(doc.uploadedAt)}
                                        </td>

                                    </tr>


                                    {
                                        selectedDocument?.id === doc.id && (

                                            <tr>
                                                <td
                                                    colSpan="5"
                                                    style={styles.expandedCell}
                                                >

                                                    <div style={styles.detailsContainer}>

                                                        <div style={styles.expandedHeader}>

                                                            <div>
                                                                <h3 style={{ margin: "0 0 5px 0" }}>
                                                                    👤 {doc.customerName}
                                                                </h3>

                                                                <p style={styles.subtitle}>
                                                                    Review submitted KYC document and verify customer identity.
                                                                </p>

                                                                {
                                                                    doc.status === "REJECTED" && doc.rejectionReason && (

                                                                        <div style={styles.rejectionInfo}>
                                                                            <strong>
                                                                                Rejection Reason:
                                                                            </strong>

                                                                            <span>
                                                                                {doc.rejectionReason}
                                                                            </span>
                                                                        </div>

                                                                    )
                                                                }
                                                            </div>


                                                            <span
                                                                style={{
                                                                    ...styles.expandedStatusBadge,
                                                                    backgroundColor:
                                                                        getStatusColor(doc.status).background,
                                                                    color:
                                                                        getStatusColor(doc.status).color
                                                                }}
                                                            >
                                                                {doc.status}
                                                            </span>

                                                        </div>


                                                        <div style={styles.reviewLayout}>

                                                            <div style={styles.infoSection}>

                                                                <h4 style={styles.sectionTitle}>
                                                                    Customer & Document Details
                                                                </h4>

                                                                <div style={styles.detailsGrid}>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            👤 Customer ID
                                                                        </span>

                                                                        <p style={styles.infoValue}>
                                                                            {doc.userId}
                                                                        </p>
                                                                    </div>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            👤 Customer Name
                                                                        </span>

                                                                        <p style={styles.infoValue}>
                                                                            {doc.customerName}
                                                                        </p>
                                                                    </div>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            ✉️ Email
                                                                        </span>
                                                                        <p style={styles.infoValue}>
                                                                            {doc.email}
                                                                        </p>
                                                                    </div>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            📄 Document Type
                                                                        </span>
                                                                        <p style={styles.infoValue}>
                                                                            {doc.documentType}
                                                                        </p>
                                                                    </div>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            📎 File Name
                                                                        </span>
                                                                        <p style={styles.infoValue}>
                                                                            {doc.originalFileName}
                                                                        </p>
                                                                    </div>

                                                                    <div style={styles.infoCard}>
                                                                        <span style={styles.infoLabel}>
                                                                            📅 Uploaded Date
                                                                        </span>
                                                                        <p style={styles.infoValue}>
                                                                            {formatDateTime(doc.uploadedAt)}
                                                                        </p>
                                                                    </div>

                                                                </div>

                                                            </div>

                                                            <div style={styles.infoSection}>

                                                                <h4 style={styles.sectionTitle}>
                                                                    🔍 OCR Extraction
                                                                </h4>
                                                                {
                                                                    extractionData && (

                                                                        <>

                                                                            <p>
                                                                                Status:
                                                                                {" "}
                                                                                <strong>
                                                                                    {extractionData.extractionStatus}
                                                                                </strong>
                                                                            </p>


                                                                            {
                                                                                extractionData?.extractionStatus === "FAILED" && (

                                                                                    <button
                                                                                        style={styles.retryButton}
                                                                                        disabled={processing}
                                                                                        onClick={() =>
                                                                                            handleRetryExtraction(doc.id)
                                                                                        }
                                                                                    >
                                                                                        {
                                                                                            processing
                                                                                                ? "Retrying..."
                                                                                                : "🔄 Retry Extraction"
                                                                                        }
                                                                                    </button>

                                                                                )
                                                                            }

                                                                        </>

                                                                    )
                                                                }

                                                            </div>

                                                            {
                                                                panData && (

                                                                    <div style={styles.infoSection}>

                                                                        <h4 style={styles.sectionTitle}>
                                                                            🪪 Extracted PAN Details
                                                                        </h4>


                                                                        <div style={styles.detailsGrid}>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    PAN Number
                                                                                </span>

                                                                                <p style={styles.documentNumberValue}>
                                                                                    {panData.panNumber}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Name
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {panData.fullName}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Father Name
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {panData.fatherName}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    DOB
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {panData.dateOfBirth}
                                                                                </p>
                                                                            </div>


                                                                        </div>

                                                                    </div>

                                                                )
                                                            }

                                                            {
                                                                aadhaarData && (

                                                                    <div style={styles.infoSection}>

                                                                        <h4 style={styles.sectionTitle}>
                                                                            🆔 Extracted Aadhaar Details
                                                                        </h4>


                                                                        <div style={styles.detailsGrid}>

                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Aadhaar Number
                                                                                </span>

                                                                                <p style={styles.documentNumberValue}>
                                                                                    {aadhaarData.aadhaarNumber}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Name
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {aadhaarData.fullName}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    DOB
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {aadhaarData.dateOfBirth}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Gender
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {aadhaarData.gender}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Mobile
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {aadhaarData.mobileNumber}
                                                                                </p>
                                                                            </div>


                                                                            <div style={styles.infoCard}>
                                                                                <span style={styles.infoLabel}>
                                                                                    Address
                                                                                </span>

                                                                                <p style={styles.infoValue}>
                                                                                    {aadhaarData.address}
                                                                                </p>
                                                                            </div>


                                                                        </div>

                                                                    </div>

                                                                )
                                                            }


                                                            <div style={styles.previewSection}>

                                                                <div style={styles.documentHeader}>

                                                                    <div>
                                                                        <strong>
                                                                            📄 {doc.documentType} Document
                                                                        </strong>

                                                                        <small style={styles.documentName}>
                                                                            {doc.originalFileName}
                                                                        </small>
                                                                    </div>


                                                                    <button
                                                                        style={styles.viewButton}
                                                                        onClick={() => {

                                                                            if (previewDocument?.id === doc.id) {
                                                                                handleHideDocument();
                                                                            } else {
                                                                                handleViewDocument(doc);
                                                                            }

                                                                        }}
                                                                    >
                                                                        {
                                                                            previewDocument?.id === doc.id
                                                                                ?
                                                                                "🙈 Hide"
                                                                                :
                                                                                "👁 View"
                                                                        }
                                                                    </button>

                                                                    <button
                                                                        style={styles.downloadButton}
                                                                        disabled={downloadingId === doc.id}
                                                                        onClick={() => handleDownloadDocument(doc)}
                                                                    >
                                                                        {downloadingId === doc.id
                                                                            ? "Downloading..."
                                                                            : "⬇ Download"
                                                                        }
                                                                    </button>
                                                                </div>


                                                                {
                                                                    previewDocument?.id === doc.id
                                                                        ?
                                                                        (
                                                                            <div style={styles.previewContainer}>

                                                                                {
                                                                                    previewDocument.type.includes("pdf")
                                                                                        ?
                                                                                        <iframe
                                                                                            src={previewDocument.url}
                                                                                            title="KYC Preview"
                                                                                            style={styles.previewFrame}
                                                                                        />
                                                                                        :
                                                                                        <img
                                                                                            src={previewDocument.url}
                                                                                            alt="KYC"
                                                                                            style={styles.previewImage}
                                                                                        />
                                                                                }

                                                                            </div>
                                                                        )
                                                                        :
                                                                        (
                                                                            <div style={styles.noPreview}>
                                                                                <span>
                                                                                    Click View to open document preview
                                                                                </span>
                                                                            </div>
                                                                        )
                                                                }
                                                                {
                                                                    doc.status === "PENDING" && (

                                                                        <div style={styles.documentActionPanel}>

                                                                            <p style={styles.actionText}>
                                                                                Review document and verify customer identity.
                                                                            </p>


                                                                            <div style={styles.detailActions}>

                                                                                <button
                                                                                    style={styles.rejectButton}
                                                                                    disabled={processing}
                                                                                    onClick={() =>
                                                                                        setRejectDocumentId(doc.id)
                                                                                    }
                                                                                >
                                                                                    Reject
                                                                                </button>


                                                                                <div style={{
                                                                                    display: "flex",
                                                                                    flexDirection: "column",
                                                                                    alignItems: "flex-end"
                                                                                }}>

                                                                                    <button
                                                                                        style={{
                                                                                            ...styles.verifyButton,
                                                                                            opacity:
                                                                                                extractionData?.extractionStatus !== "SUCCESS"
                                                                                                    ? 0.5
                                                                                                    : 1,
                                                                                            cursor:
                                                                                                extractionData?.extractionStatus !== "SUCCESS"
                                                                                                    ? "not-allowed"
                                                                                                    : "pointer"
                                                                                        }}
                                                                                        disabled={
                                                                                            processing ||
                                                                                            extractionData?.extractionStatus !== "SUCCESS"
                                                                                        }
                                                                                        title={
                                                                                            getVerificationMessage()
                                                                                        }
                                                                                        onClick={() =>
                                                                                            handleVerify(doc.id)
                                                                                        }
                                                                                    >
                                                                                        Verify
                                                                                    </button>


                                                                                    {
                                                                                        getVerificationMessage() && (

                                                                                            <span style={styles.verifyHint}>
                                                                                                {getVerificationMessage()}
                                                                                            </span>

                                                                                        )
                                                                                    }

                                                                                </div>

                                                                            </div>

                                                                        </div>

                                                                    )
                                                                }
                                                            </div>
                                                        </div>
                                                    </div>

                                                </td>
                                            </tr>

                                        )
                                    }

                                </React.Fragment>
                            ))
                        }
                    </tbody>
                </table>
            </div>

            <KycPagination
                totalPages={totalPages}
                page={page}
                setPage={setPage}
            />

            {
                rejectDocumentId && (

                    <div style={styles.modalOverlay}>

                        <div style={styles.modal}>

                            <h3>
                                Reject Document
                            </h3>


                            <textarea
                                style={styles.textarea}
                                placeholder="Enter rejection reason"
                                value={rejectionReason}
                                onChange={(e) =>
                                    setRejectionReason(e.target.value)
                                }
                            />


                            <div style={styles.modalActions}>

                                <button
                                    style={styles.cancelButton}
                                    onClick={() => {
                                        setRejectDocumentId(null);
                                        setRejectionReason("");
                                    }}
                                >
                                    Cancel
                                </button>


                                <button
                                    style={styles.rejectButton}
                                    onClick={handleReject}
                                >
                                    Reject
                                </button>


                            </div>

                        </div>

                    </div>

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

    title: {
        fontSize: "24px",
        fontFamily: "Georgia, serif"
    },

    summaryGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(5, 1fr)",
        gap: "18px",
        marginBottom: "26px",
        width: "100%"
    },

    summaryCard: {
        borderRadius: "12px",
        padding: "16px",
        background: "#ffffff",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start"
    },

    summaryTitle: {
        display: "flex",
        alignItems: "center",
        gap: "8px",
        fontWeight: "600",
        color: "#334155",
        marginBottom: "18px"
    },

    summaryValue: {
        fontSize: "26px",
        fontWeight: "700",
        color: "#0f172a"
    },

    card: {
        background: "#ffffff",
        padding: "20px",
        borderRadius: "14px"
    },

    table: {
        width: "100%",
        borderCollapse: "collapse",
    },

    th: {
        textAlign: "left",
        padding: "14px 12px",
        borderBottom: "1px solid #e2e8f0",
        fontSize: "12px",
        color: "#64748b",
        fontWeight: "700",
        textTransform: "uppercase",
        letterSpacing: "0.5px",
        background: "#f8fafc"
    },

    td: {
        padding: "14px 12px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155"
    },

    previewContainer: {
        marginTop: "16px",
        border: "1px solid #e2e8f0",
        borderRadius: "12px",
        padding: "12px",
        background: "#f8fafc"
    },

    previewFrame: {
        width: "100%",
        height: "280px",
        border: "none"
    },

    previewImage: {
        width: "100%",
        maxHeight: "280px",
        objectFit: "contain"
    },

    searchButton: {
        padding: "10px 18px",
        background: "#0d6360",
        color: "#ffffff",
        border: "none",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "600"
    },

    headerRow: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center"
    },

    refreshButton: {
        background: "#0d6360",
        color: "#ffffff",
        border: "none",
        padding: "10px 18px",
        borderRadius: "8px",
        cursor: "pointer",
        fontWeight: "700"
    },

    statusBadge: {
        padding: "5px 12px",
        borderRadius: "20px",
        fontSize: "12px",
        fontWeight: "700"
    },

    actionGroup: {
        display: "flex",
        gap: "8px"
    },

    viewButton: {
        padding: "7px 12px",
        borderRadius: "7px",
        border: "1px solid #64748b",
        background: "#ffffff",
        cursor: "pointer"
    },

    verifyButton: {
        padding: "10px 22px",
        borderRadius: "8px",
        border: "none",
        background: "#16a34a",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
        boxShadow: "0 2px 5px rgba(22,163,74,0.25)"
    },

    rejectButton: {
        padding: "10px 22px",
        borderRadius: "8px",
        border: "none",
        background: "#dc2626",
        color: "#ffffff",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
        boxShadow: "0 2px 5px rgba(220,38,38,0.25)"
    },

    modalOverlay: {
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.4)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000
    },

    modal: {
        background: "#ffffff",
        padding: "24px",
        borderRadius: "12px",
        width: "400px"
    },

    textarea: {
        width: "100%",
        height: "120px",
        marginTop: "15px",
        padding: "10px",
        borderRadius: "8px",
        border: "1px solid #cbd5e1"
    },

    modalActions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "10px",
        marginTop: "15px"
    },

    cancelButton: {
        padding: "8px 14px",
        borderRadius: "7px",
        border: "1px solid #cbd5e1",
        background: "#ffffff",
        cursor: "pointer"
    },

    expandedCell: {
        padding: "0",
        background: "#f8fafc"
    },

    detailsContainer: {
        padding: "16px",
        margin: "8px",
        background: "#ffffff",
        borderRadius: "12px",
        border: "1px solid #e2e8f0",
        boxShadow: "0 2px 8px rgba(0,0,0,0.04)"
    },

    detailsGrid: {
        display: "grid",
        gridTemplateColumns: "repeat(3,1fr)",
        gap: "14px",
        marginTop: "12px"
    },

    detailActions: {
        display: "flex",
        justifyContent: "flex-end",
        gap: "12px",
        marginTop: "12px"
    },

    subtitle: {
        color: "#64748b",
        margin: "0 0 10px 0",
        fontSize: "14px"
    },

    sectionTitle: {
        margin: "0 0 10px 0",
        fontSize: "16px"
    },

    reviewLayout: {
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "25px"
    },

    infoSection: {
        background: "#f8fafc",
        padding: "12px",
        borderRadius: "12px"
    },

    previewSection: {
        background: "#f8fafc",
        padding: "12px",
        borderRadius: "12px",
        display: "flex",
        flexDirection: "column",
        gap: "8px"
    },

    verificationSection: {
        marginTop: "12px",
        paddingTop: "10px",
        borderTop: "1px solid #e2e8f0",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center"
    },

    infoCard: {
        background: "#ffffff",
        border: "1px solid #e2e8f0",
        borderRadius: "10px",
        padding: "12px",
        display: "flex",
        flexDirection: "column",
        gap: "6px"
    },

    infoLabel: {
        color: "#64748b",
        fontSize: "12px",
        fontWeight: "600"
    },

    infoValue: {
        margin: 0,
        fontSize: "14px",
        fontWeight: "700",
        color: "#0f172a"
    },

    documentHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "10px"
    },

    noPreview: {
        height: "120px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#64748b",
        background: "#ffffff",
        border: "1px dashed #cbd5e1",
        borderRadius: "10px",
        fontSize: "14px"
    },

    documentName: {
        display: "block",
        marginTop: "4px",
        color: "#64748b",
        fontSize: "12px"
    },

    documentActionPanel: {
        marginTop: "14px",
        paddingTop: "12px",
        borderTop: "1px solid #e2e8f0",
    },

    actionTitle: {
        margin: 0,
        fontSize: "15px",
        color: "#0f172a"
    },

    actionText: {
        margin: "0",
        fontSize: "13px",
        color: "#64748b",
        lineHeight: "1.5"
    },

    customerName: {
        fontSize: "14px",
        color: "#0f172a"
    },

    toolbarWrapper: {
        position: "relative",
    },

    loadingText: {
        position: "absolute",
        right: "0",
        top: "-18px",
        fontSize: "12px",
        color: "#64748b",
        fontWeight: "600"
    },

    expandedHeader: {
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: "14px"
    },

    expandedStatusBadge: {
        padding: "8px 16px",
        borderRadius: "20px",
        fontSize: "13px",
        fontWeight: "700",
        textTransform: "uppercase"
    },

    rejectionInfo: {
        marginTop: "8px",
        padding: "10px 12px",
        background: "#FEF2F2",
        border: "1px solid #FECACA",
        borderRadius: "8px",
        color: "#991B1B",
        fontSize: "13px",
        display: "flex",
        gap: "8px",
        alignItems: "center"
    },

    documentNumberValue: {
        margin: 0,
        fontSize: "16px",
        fontWeight: "800",
        color: "#0d6360",
        background: "#ecfdf5",
        padding: "8px 12px",
        borderRadius: "8px",
        letterSpacing: "1.5px",
        display: "inline-block"
    },

    retryButton: {
        marginTop: "12px",
        padding: "8px 16px",
        borderRadius: "8px",
        border: "1px solid #0d6360",
        background: "#ffffff",
        color: "#0d6360",
        cursor: "pointer",
        fontWeight: "700"
    },

    downloadButton: {
        padding: "7px 14px",
        borderRadius: "7px",
        border: "1px solid #0d6360",
        background: "#ffffff",
        color: "#0d6360",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "13px",
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        transition: "all 0.2s ease"
    },

    verifyHint: {
        marginTop: "8px",
        fontSize: "12px",
        color: "#92400e",
        background: "#fef3c7",
        border: "1px solid #fde68a",
        padding: "6px 10px",
        borderRadius: "6px",
        fontWeight: "600",
        maxWidth: "250px",
        textAlign: "right"
    },

};


export default KycManagementView;