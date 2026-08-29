import React from "react";
import { formatDateTime } from "../../utils/formatUtils";
import { getStatusColor } from "./kycUtils";

const KycDocumentRow = ({
    document,
    selected,
    onSelect,
    renderDetails
}) => {
    const statusStyle = getStatusColor(document.status);

    return (
        <>
            <tr
                style={{
                    ...styles.row,
                    backgroundColor: selected
                        ? "#ecfeff"
                        : "#ffffff",
                    borderLeft: selected
                        ? "3px solid #0d9488"
                        : "3px solid transparent"
                }}
                onMouseEnter={(event) => {
                    if (!selected) {
                        event.currentTarget.style.backgroundColor =
                            "#f8fafc";
                    }
                }}
                onMouseLeave={(event) => {
                    if (!selected) {
                        event.currentTarget.style.backgroundColor =
                            "#ffffff";
                    }
                }}
                onClick={() => onSelect(document)}
            >
                <td style={styles.td}>
                    <strong style={styles.customerName}>
                        {document.customerName}
                    </strong>

                    <br />

                    <small>
                        {document.email}
                    </small>
                </td>

                <td style={styles.td}>
                    {document.originalFileName}
                </td>

                <td style={styles.td}>
                    {document.documentType}
                </td>

                <td style={styles.td}>
                    <span
                        style={{
                            ...styles.statusBadge,
                            backgroundColor: statusStyle.background,
                            color: statusStyle.color
                        }}
                    >
                        {document.status}
                    </span>
                </td>

                <td style={styles.td}>
                    {formatDateTime(document.uploadedAt)}
                </td>
            </tr>

            {selected && (
                <tr>
                    <td
                        colSpan={5}
                        style={styles.detailsCell}
                    >
                        {renderDetails(document)}
                    </td>
                </tr>
            )}
        </>
    );
};

const styles = {
    row: {
        cursor: "pointer",
        transition: "background-color 0.2s ease"
    },

    td: {
        padding: "14px 12px",
        borderBottom: "1px solid #f1f5f9",
        fontSize: "14px",
        color: "#334155"
    },

    customerName: {
        fontSize: "14px",
        color: "#0f172a"
    },

    statusBadge: {
        padding: "5px 12px",
        borderRadius: "20px",
        fontSize: "12px",
        fontWeight: "700"
    },

    detailsCell: {
        padding: 0,
        background: "#f8fafc"
    }
};

export default KycDocumentRow;