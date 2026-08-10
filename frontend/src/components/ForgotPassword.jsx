import React, { useState } from "react";
import { forgotPassword } from "../api/bankService";
import toast from "react-hot-toast";

const ForgotPassword = ({ onBackToLogin }) => {

    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {

        e.preventDefault();

        setLoading(true);

        try {

            await forgotPassword({ email });

            setSuccess(true);

            toast.success(
                "If an account exists, a reset link has been sent."
            );

        } catch (err) {

            toast.error(
                err.response?.data?.message ||
                "Something went wrong."
            );

        } finally {

            setLoading(false);

        }
    };

    if (success) {

        return (
            <div style={styles.container}>
                <div style={styles.card}>

                    <h2 style={styles.brand}>
                        🏦 BankFlow
                    </h2>

                    <h3>📧 Check your email</h3>

                    <p style={styles.subtitle}>
                        If an account exists for this email,
                        we've sent a password reset link.
                    </p>

                    <button
                        style={styles.submitBtn}
                        onClick={onBackToLogin}
                    >
                        Back to Login
                    </button>

                </div>
            </div>
        );
    }

    return (

        <div style={styles.container}>

            <div style={styles.card}>

                <h2 style={styles.brand}>
                    🏦 BankFlow
                </h2>

                <p style={styles.subtitle}>
                    Forgot your password?
                </p>

                <form
                    onSubmit={handleSubmit}
                    style={styles.form}
                >

                    <div style={styles.field}>

                        <label style={styles.label}>
                            Email Address
                        </label>

                        <input
                            type="email"
                            required
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                            style={styles.input}
                        />

                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        style={styles.submitBtn}
                    >
                        {loading
                            ? "Sending..."
                            : "Send Reset Link"}
                    </button>

                    <button
                        type="button"
                        onClick={onBackToLogin}
                        style={styles.linkBtn}
                    >
                        Back to Login
                    </button>

                </form>

            </div>

        </div>

    );

};

const styles = {
    container: {
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        minHeight: "100vh",
        background: "#f9fafb",
        padding: "16px",
    },
    card: {
        background: "#fff",
        padding: "32px",
        borderRadius: "16px",
        width: "100%",
        maxWidth: "420px",
        boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
    },
    brand: {
        textAlign: "center",
        color: "#0d6360",
    },
    subtitle: {
        textAlign: "center",
        color: "#6b7280",
        marginBottom: "20px",
    },
    form: {
        display: "flex",
        flexDirection: "column",
        gap: "16px",
    },
    field: {
        display: "flex",
        flexDirection: "column",
        gap: "6px",
    },
    label: {
        fontWeight: 600,
    },
    input: {
        padding: "12px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
    },
    submitBtn: {
        background: "#0d6360",
        color: "#fff",
        border: "none",
        padding: "12px",
        borderRadius: "8px",
        cursor: "pointer",
    },
    linkBtn: {
        border: "none",
        background: "none",
        color: "#0d6360",
        cursor: "pointer",
    },
};

export default ForgotPassword;