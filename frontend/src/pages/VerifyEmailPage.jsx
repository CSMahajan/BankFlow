import React, { useEffect, useState, useRef } from "react";
import { verifyEmail } from "../api/bankService";

const VerifyEmailPage = ({
    token,
    onGoToLogin,
}) => {

    const [loading, setLoading] = useState(true);
    const [success, setSuccess] = useState(false);
    const [message, setMessage] = useState("");
    const hasCalled = useRef(false);

    useEffect(() => {
        if (hasCalled.current) {
            return;
        }
        hasCalled.current = true;
        const verify = async () => {
            try {
                const response = await verifyEmail(token);
                setSuccess(true);
                setMessage(response);
            } catch (err) {
                setSuccess(false);
                setMessage(
                    err.response?.data?.message ||
                    "Email verification failed."
                );
            } finally {
                setLoading(false);
            }
        };
        verify();
    }, [token]);

    return (
        <div style={styles.container}>
            <div style={styles.card}>

                <div style={styles.header}>
                    <h2 style={styles.brand}>🏦 BankFlow</h2>
                </div>

                {loading ? (
                    <>
                        <h2 style={styles.title}>
                            Verifying Email...
                        </h2>

                        <p style={styles.message}>
                            Please wait while we verify your email.
                        </p>
                    </>
                ) : success ? (
                    <>
                        <div style={styles.icon}>
                            ✅
                        </div>

                        <h2
                            style={{
                                ...styles.title,
                                color: "#15803d",
                            }}
                        >
                            Email Verified Successfully
                        </h2>

                        <p style={styles.message}>
                            {success
                                ? "Your email address has been verified successfully. You can now securely sign in to your BankFlow account."
                                : message}
                        </p>

                        <button
                            style={styles.button}
                            onClick={onGoToLogin}
                        >
                            Continue to Login
                        </button>
                    </>
                ) : (
                    <>
                        <div style={styles.icon}>
                            ❌
                        </div>

                        <h2
                            style={{
                                ...styles.title,
                                color: "#b91c1c",
                            }}
                        >
                            Verification Failed
                        </h2>

                        <p style={styles.message}>
                            {message}
                        </p>

                        <button
                            style={styles.button}
                            onClick={onGoToLogin}
                        >
                            Return to Login
                        </button>
                    </>
                )}

            </div>
        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        minHeight: "100vh",
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#f9fafb",
        padding: "16px",
    },

    card: {
        backgroundColor: "#ffffff",
        borderRadius: "16px",
        padding: "40px",
        width: "100%",
        maxWidth: "420px",
        border: "1px solid #eef0ec",
        boxShadow: "0 4px 12px rgba(0,0,0,0.03)",
        textAlign: "center",
    },

    header: {
        marginBottom: "24px",
    },

    brand: {
        fontSize: "24px",
        fontWeight: "800",
        fontFamily: "Georgia, serif",
        color: "#0d6360",
    },

    icon: {
        fontSize: "56px",
        marginBottom: "18px",
    },

    title: {
        fontSize: "24px",
        marginBottom: "12px",
        color: "#111827",
    },

    message: {
        color: "#6b7280",
        lineHeight: 1.6,
        marginBottom: "30px",
    },

    button: {
        width: "100%",
        backgroundColor: "#0d6360",
        color: "#fff",
        border: "none",
        borderRadius: "8px",
        padding: "12px",
        cursor: "pointer",
        fontWeight: "700",
        fontSize: "14px",
    },
};

export default VerifyEmailPage;