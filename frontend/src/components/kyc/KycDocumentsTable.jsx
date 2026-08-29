import React from "react";
import KycDocumentRow from "./KycDocumentRow";

const KycDocumentsTable = ({
    documents,
    selectedDocument,
    loading,
    onSelect,
    renderDetails
}) => {
    return (
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
                        <th style={styles.th}>Customer</th>
                        <th style={styles.th}>Document</th>
                        <th style={styles.th}>Type</th>
                        <th style={styles.th}>Status</th>
                        <th style={styles.th}>Uploaded</th>
                    </tr>
                </thead>

                <tbody>
                    {documents.map((document) => (
                        <KycDocumentRow
                            key={document.id}
                            document={document}
                            selected={
                                selectedDocument?.id === document.id
                            }
                            onSelect={onSelect}
                            renderDetails={renderDetails}
                        />
                    ))}
                </tbody>
            </table>
        </div>
    );
};

const styles = {
    card: {
        background: "#ffffff",
        padding: "20px",
        borderRadius: "14px"
    },

    table: {
        width: "100%",
        borderCollapse: "collapse"
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
    }
};

export default KycDocumentsTable;