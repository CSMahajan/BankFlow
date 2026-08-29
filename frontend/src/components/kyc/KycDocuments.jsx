import React from "react";
import DocumentCard from "./DocumentCard";

const KycDocuments = ({
    documents,
    uploadingType,
    onUpload,
    onView,
    previewDocument,
    onHide,
    selectedFiles,
    getStatusColor,
    styles
}) => {

    const panDocument = documents.find(
        d => d.documentType === "PAN"
    );

    const aadhaarDocument = documents.find(
        d => d.documentType === "AADHAAR"
    );

    return (
        <>
            <h3 style={styles.documentsTitle}>
                Required Documents
            </h3>

            <div style={styles.grid}>

                <DocumentCard
                    title="PAN Card"
                    icon="🪪"
                    type="PAN"
                    document={panDocument}
                    uploadingType={uploadingType}
                    onUpload={onUpload}
                    onView={onView}
                    previewDocument={previewDocument}
                    onHide={onHide}
                    selectedFile={selectedFiles["PAN"]}
                    getStatusColor={getStatusColor}
                    styles={styles}
                />

                <DocumentCard
                    title="Aadhaar Card"
                    icon="🆔"
                    type="AADHAAR"
                    document={aadhaarDocument}
                    uploadingType={uploadingType}
                    onUpload={onUpload}
                    onView={onView}
                    previewDocument={previewDocument}
                    onHide={onHide}
                    selectedFile={selectedFiles["AADHAAR"]}
                    getStatusColor={getStatusColor}
                    styles={styles}
                />

            </div>
        </>
    );
};

export default KycDocuments;