import React, { useEffect, useState } from "react";
import {
    fetchCurrentUser,
    updateProfile,
    changePassword,
} from "../api/bankService";
import {
    EyeIcon,
    EyeSlashIcon,
} from "@heroicons/react/24/outline";
import toast from "react-hot-toast";

const ProfileView = ({ onProfileUpdated }) => {
    const [profile, setProfile] = useState(null);
    const [fullName, setFullName] = useState("");

    const [loadingProfile, setLoadingProfile] = useState(true);
    const [savingProfile, setSavingProfile] = useState(false);

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [changingPassword, setChangingPassword] = useState(false);

    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    useEffect(() => {
        loadProfile();
    }, []);

    const loadProfile = async () => {
        try {
            setLoadingProfile(true);

            const data = await fetchCurrentUser();

            setProfile(data);
            setFullName(data.fullName || "");
        } catch (err) {
            console.error("Failed to load profile:", err);

            toast.error(
                err.response?.data?.message ||
                "Unable to load your profile."
            );
        } finally {
            setLoadingProfile(false);
        }
    };

    const handleProfileUpdate = async (e) => {
        e.preventDefault();

        if (!fullName.trim()) {
            toast.error("Full name is required.");
            return;
        }

        try {
            setSavingProfile(true);

            await updateProfile({
                fullName: fullName.trim(),
            });

            // Keep localStorage synchronized with the database.
            localStorage.setItem("fullName", fullName.trim());

            // Tell the parent dashboard that the name changed.
            if (onProfileUpdated) {
                onProfileUpdated(fullName.trim());
            }

            setProfile((previous) => ({
                ...previous,
                fullName: fullName.trim(),
            }));

            toast.success("Profile updated successfully.");
        } catch (err) {
            console.error("Profile update failed:", err);

            toast.error(
                err.response?.data?.message ||
                "Unable to update your profile."
            );
        } finally {
            setSavingProfile(false);
        }
    };

    const handleChangePassword = async (e) => {
        e.preventDefault();

        if (newPassword !== confirmPassword) {
            toast.error("New password and confirm password do not match.");
            return;
        }

        if (newPassword.length < 8) {
            toast.error("New password must be at least 8 characters.");
            return;
        }

        try {
            setChangingPassword(true);

            await changePassword({
                currentPassword,
                newPassword,
                confirmPassword,
            });

            toast.success("Password changed successfully.");

            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
        } catch (err) {
            console.error("Password change failed:", err);

            toast.error(
                err.response?.data?.message ||
                "Unable to change password."
            );
        } finally {
            setChangingPassword(false);
        }
    };

    const passwordField = (
        label,
        value,
        setValue,
        showPassword,
        setShowPassword,
        placeholder
    ) => (
        <div style={styles.field}>
            <label style={styles.label}>{label}</label>

            <div style={styles.passwordWrapper}>
                <input
                    type={showPassword ? "text" : "password"}
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    placeholder={placeholder}
                    required
                    style={styles.passwordInput}
                />

                <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={styles.eyeButton}
                >
                    {showPassword ? (
                        <EyeSlashIcon style={styles.eyeIcon} />
                    ) : (
                        <EyeIcon style={styles.eyeIcon} />
                    )}
                </button>
            </div>
        </div>
    );

    if (loadingProfile) {
        return (
            <div style={styles.loadingContainer}>
                <p style={styles.mutedText}>Loading profile...</p>
            </div>
        );
    }

    if (!profile) {
        return (
            <div style={styles.errorCard}>
                <p>Unable to load profile.</p>
            </div>
        );
    }

    return (
        <div style={styles.container}>

            {/* Page Header */}
            <div style={styles.pageHeader}>
                <div>
                    <h2 style={styles.pageTitle}>My Profile</h2>
                    <p style={styles.pageSubtitle}>
                        Manage your personal information and account security.
                    </p>
                </div>
            </div>

            {/* Personal Information */}
            <div style={styles.card}>
                <div style={styles.cardHeader}>
                    <div style={styles.cardIcon}>👤</div>

                    <div>
                        <h3 style={styles.cardTitle}>
                            Personal Information
                        </h3>

                        <p style={styles.cardSubtitle}>
                            View and update your account information.
                        </p>
                    </div>
                </div>

                <form onSubmit={handleProfileUpdate}>
                    <div style={styles.field}>
                        <label style={styles.label}>
                            Full Name
                        </label>

                        <input
                            type="text"
                            value={fullName}
                            onChange={(e) => setFullName(e.target.value)}
                            style={styles.input}
                            required
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>
                            Email Address
                        </label>

                        <input
                            type="email"
                            value={profile.email}
                            disabled
                            style={{
                                ...styles.input,
                                backgroundColor: "#f3f4f6",
                                color: "#6b7280",
                                cursor: "not-allowed",
                            }}
                        />

                        <span style={styles.helperText}>
                            Email address cannot be changed.
                        </span>
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>
                            Role
                        </label>

                        <div style={styles.roleBadge}>
                            {profile.role}
                        </div>
                    </div>

                    <div style={styles.buttonRow}>
                        <button
                            type="submit"
                            disabled={savingProfile}
                            style={styles.primaryButton}
                        >
                            {savingProfile
                                ? "Saving..."
                                : "Save Changes"}
                        </button>
                    </div>
                </form>
            </div>

            {/* Security */}
            <div style={styles.card}>
                <div style={styles.cardHeader}>
                    <div style={styles.cardIcon}>🔐</div>

                    <div>
                        <h3 style={styles.cardTitle}>
                            Security
                        </h3>

                        <p style={styles.cardSubtitle}>
                            Change your password to keep your account secure.
                        </p>
                    </div>
                </div>

                <form onSubmit={handleChangePassword}>

                    {passwordField(
                        "Current Password",
                        currentPassword,
                        setCurrentPassword,
                        showCurrentPassword,
                        setShowCurrentPassword,
                        "Enter your current password"
                    )}

                    {passwordField(
                        "New Password",
                        newPassword,
                        setNewPassword,
                        showNewPassword,
                        setShowNewPassword,
                        "Enter your new password"
                    )}

                    {passwordField(
                        "Confirm New Password",
                        confirmPassword,
                        setConfirmPassword,
                        showConfirmPassword,
                        setShowConfirmPassword,
                        "Confirm your new password"
                    )}

                    <p style={styles.passwordHint}>
                        Password must be at least 8 characters.
                    </p>

                    <div style={styles.buttonRow}>
                        <button
                            type="submit"
                            disabled={changingPassword}
                            style={styles.primaryButton}
                        >
                            {changingPassword
                                ? "Changing Password..."
                                : "Change Password"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

const styles = {
    container: {
        display: "flex",
        flexDirection: "column",
        gap: "20px",
    },

    loadingContainer: {
        padding: "40px",
        textAlign: "center",
    },

    pageHeader: {
        marginBottom: "4px",
    },

    pageTitle: {
        margin: 0,
        fontSize: "24px",
        fontFamily: "Georgia, serif",
        color: "#111827",
    },

    pageSubtitle: {
        margin: "5px 0 0 0",
        fontSize: "13px",
        color: "#6b7280",
    },

    card: {
        backgroundColor: "#ffffff",
        borderRadius: "12px",
        padding: "24px",
        border: "1px solid #eef0ec",
        boxShadow: "0 2px 6px rgba(0,0,0,0.03)",
    },

    cardHeader: {
        display: "flex",
        alignItems: "center",
        gap: "12px",
        marginBottom: "24px",
    },

    cardIcon: {
        width: "40px",
        height: "40px",
        borderRadius: "10px",
        backgroundColor: "#e6f2f1",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontSize: "20px",
    },

    cardTitle: {
        margin: 0,
        fontSize: "17px",
        color: "#111827",
    },

    cardSubtitle: {
        margin: "3px 0 0 0",
        fontSize: "12px",
        color: "#6b7280",
    },

    field: {
        display: "flex",
        flexDirection: "column",
        gap: "6px",
        marginBottom: "18px",
    },

    label: {
        fontSize: "13px",
        fontWeight: "700",
        color: "#374151",
    },

    input: {
        width: "100%",
        boxSizing: "border-box",
        padding: "11px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
        outline: "none",
    },

    passwordWrapper: {
        position: "relative",
        width: "100%",
    },

    passwordInput: {
        width: "100%",
        boxSizing: "border-box",
        padding: "11px 48px 11px 14px",
        borderRadius: "8px",
        border: "1px solid #d1d5db",
        fontSize: "14px",
        outline: "none",
    },

    eyeButton: {
        position: "absolute",
        right: "14px",
        top: "50%",
        transform: "translateY(-50%)",
        border: "none",
        background: "transparent",
        padding: 0,
        cursor: "pointer",
        color: "#6b7280",
        display: "flex",
        alignItems: "center",
    },

    eyeIcon: {
        width: "21px",
        height: "21px",
    },

    helperText: {
        fontSize: "11px",
        color: "#6b7280",
    },

    roleBadge: {
        width: "fit-content",
        padding: "5px 9px",
        borderRadius: "6px",
        backgroundColor: "#e6f2f1",
        color: "#0d6360",
        fontSize: "11px",
        fontWeight: "700",
    },

    passwordHint: {
        margin: "-6px 0 18px 0",
        fontSize: "11px",
        color: "#6b7280",
    },

    buttonRow: {
        display: "flex",
        justifyContent: "flex-end",
    },

    primaryButton: {
        backgroundColor: "#0d6360",
        color: "#ffffff",
        border: "none",
        padding: "11px 18px",
        borderRadius: "8px",
        fontWeight: "700",
        fontSize: "13px",
        cursor: "pointer",
    },

    errorCard: {
        backgroundColor: "#fee2e2",
        color: "#991b1b",
        padding: "16px",
        borderRadius: "8px",
    },

    mutedText: {
        color: "#6b7280",
    },
};

export default ProfileView;