import React, { useState } from "react";
import { resetPassword } from "../api/bankService";
import { EyeIcon, EyeSlashIcon } from "@heroicons/react/24/outline";

const ResetPasswordPage = ({ token }) => {

    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [message, setMessage] = useState("");

    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const handleSubmit = async (e) => {

        e.preventDefault();

        setLoading(true);

        try {

            await resetPassword({

                token,
                newPassword,
                confirmPassword

            });

            setSuccess(true);

            setMessage("Your password has been reset successfully.");

        } catch (err) {

            setMessage(
                err.response?.data?.message ||
                "Password reset failed."
            );

        } finally {

            setLoading(false);

        }

    };

    return (

        <div style={styles.container}>

            <div style={styles.card}>

                <h2 style={styles.brand}>
                    🏦 BankFlow
                </h2>

                {success ? (

                    <>
                        <h3>✅ Password Reset Successful</h3>

                        <p>{message}</p>

                        <button
                            style={styles.submitBtn}
                            onClick={() => {

                                window.location.replace("/");

                            }}
                        >
                            Continue to Login
                        </button>

                    </>

                ) : (

                    <>

                        <h3>Reset Password</h3>

                        {message && (
                            <div style={styles.errorBox}>
                                {message}
                            </div>
                        )}

                        <form
                            onSubmit={handleSubmit}
                            style={styles.form}
                        >

                            {/* New Password */}

                            <div style={styles.field}>

                                <label style={styles.label}>
                                    New Password
                                </label>

                                <div style={styles.passwordWrapper}>

                                    <input
                                        type={
                                            showNewPassword
                                                ? "text"
                                                : "password"
                                        }
                                        value={newPassword}
                                        onChange={(e) =>
                                            setNewPassword(
                                                e.target.value
                                            )
                                        }
                                        required
                                        style={styles.input}
                                    />

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowNewPassword(
                                                !showNewPassword
                                            )
                                        }
                                        style={styles.eyeButton}
                                    >
                                        {showNewPassword
                                            ? <EyeSlashIcon width={22} />
                                            : <EyeIcon width={22} />}
                                    </button>

                                </div>

                            </div>

                            {/* Confirm Password */}

                            <div style={styles.field}>

                                <label style={styles.label}>
                                    Confirm Password
                                </label>

                                <div style={styles.passwordWrapper}>

                                    <input
                                        type={
                                            showConfirmPassword
                                                ? "text"
                                                : "password"
                                        }
                                        value={confirmPassword}
                                        onChange={(e) =>
                                            setConfirmPassword(
                                                e.target.value
                                            )
                                        }
                                        required
                                        style={styles.input}
                                    />

                                    <button
                                        type="button"
                                        onClick={() =>
                                            setShowConfirmPassword(
                                                !showConfirmPassword
                                            )
                                        }
                                        style={styles.eyeButton}
                                    >
                                        {showConfirmPassword
                                            ? <EyeSlashIcon width={22} />
                                            : <EyeIcon width={22} />}
                                    </button>

                                </div>

                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                style={styles.submitBtn}
                            >
                                {loading
                                    ? "Resetting..."
                                    : "Reset Password"}
                            </button>

                        </form>

                    </>

                )}

            </div>

        </div>

    );

};

const styles = {
    container: { display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', backgroundColor: '#f9fafb', padding: '16px' },
    card: { backgroundColor: '#ffffff', borderRadius: '16px', padding: '32px', width: '100%', maxWidth: '420px', border: '1px solid #eef0ec', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' },
    header: { textAlign: 'center', marginBottom: '24px' },
    brand: { fontSize: '24px', fontWeight: '800', fontFamily: 'Georgia, serif', color: '#0d6360', margin: '0 0 6px 0' },
    subtitle: { fontSize: '14px', color: '#6b7280', margin: 0 },
    roleTabs: { display: 'flex', gap: '8px', marginBottom: '20px', backgroundColor: '#f3f4f6', padding: '4px', borderRadius: '10px' },
    roleTabBtn: { flex: 1, border: 'none', padding: '10px', borderRadius: '8px', fontWeight: '700', fontSize: '13px', cursor: 'pointer', transition: 'all 0.2s' },
    form: { display: 'flex', flexDirection: 'column', gap: '16px' },
    field: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '13px', fontWeight: '700', color: '#374151' },
    input: { padding: '11px 14px', borderRadius: '8px', border: '1px solid #d1d5db', fontSize: '14px', outline: 'none' },
    submitBtn: { backgroundColor: '#0d6360', color: '#ffffff', border: 'none', padding: '12px', borderRadius: '8px', fontWeight: '700', fontSize: '14px', cursor: 'pointer', marginTop: '4px' },
    errorBox: { backgroundColor: '#fee2e2', color: '#991b1b', padding: '10px 14px', borderRadius: '8px', fontSize: '13px', marginBottom: '16px' },
    footerPrompt: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px', marginTop: '20px', fontSize: '13px', color: '#6b7280' },
    linkBtn: { background: 'none', border: 'none', color: '#0d6360', fontWeight: '700', cursor: 'pointer', padding: 0, fontSize: '13px' },
    passwordWrapper: {
        position: "relative",
    },

    eyeButton: {
        position: "absolute",
        right: "14px",
        top: "50%",
        transform: "translateY(-50%)",
        border: "none",
        background: "transparent",
        cursor: "pointer",
        color: "#6b7280",
    },
};

export default ResetPasswordPage;