import React, { useState } from "react";
import { askAIAssistant } from "../../api/bankService";

const AIAssistant = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [question, setQuestion] = useState("");
    const [messages, setMessages] = useState([
        {
            id: 1,
            role: "assistant",
            content:
                "Hi! I'm your BankFlow Assistant. I can help you with your accounts, transactions, cards, loans, fixed deposits, and BankFlow features.",
        },
    ]);
    const [loading, setLoading] = useState(false);

    const suggestedQuestions = [
        "What are my bank accounts?",
        "Show me my recent transactions",
        "What cards do I have?",
        "What loans do I have?",
    ];

    const handleAsk = async (text = question) => {
        const trimmedQuestion = text.trim();

        if (!trimmedQuestion || loading) {
            return;
        }

        const userMessage = {
            id: Date.now(),
            role: "user",
            content: trimmedQuestion,
        };

        setMessages((previous) => [...previous, userMessage]);
        setQuestion("");
        setLoading(true);

        try {
            const response = await askAIAssistant(trimmedQuestion);

            const assistantMessage = {
                id: Date.now() + 1,
                role: "assistant",
                content:
                    response?.answer ||
                    "Sorry, I couldn't generate a response right now.",
            };

            setMessages((previous) => [...previous, assistantMessage]);
        } catch (error) {
            console.error("AI Assistant request failed:", error);

            setMessages((previous) => [
                ...previous,
                {
                    id: Date.now() + 1,
                    role: "assistant",
                    content:
                        "Sorry, I couldn't process your request right now. Please try again.",
                },
            ]);
        } finally {
            setLoading(false);
        }
    };

    const handleKeyDown = (event) => {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();
            handleAsk();
        }
    };

    return (
        <>
            {isOpen && (
                <div style={styles.chatPanel}>
                    <div style={styles.header}>
                        <div>
                            <div style={styles.headerTitle}>BankFlow Assistant</div>
                            <div style={styles.headerSubtitle}>
                                Your banking assistant
                            </div>
                        </div>

                        <button
                            type="button"
                            onClick={() => setIsOpen(false)}
                            style={styles.closeButton}
                            aria-label="Close AI Assistant"
                        >
                            ×
                        </button>
                    </div>

                    <div style={styles.messagesContainer}>
                        {messages.map((message) => (
                            <div
                                key={message.id}
                                style={{
                                    ...styles.messageRow,
                                    justifyContent:
                                        message.role === "user" ? "flex-end" : "flex-start",
                                }}
                            >
                                <div
                                    style={
                                        message.role === "user"
                                            ? styles.userMessage
                                            : styles.assistantMessage
                                    }
                                >
                                    {message.content}
                                </div>
                            </div>
                        ))}

                        {loading && (
                            <div style={styles.messageRow}>
                                <div style={styles.assistantMessage}>
                                    Thinking...
                                </div>
                            </div>
                        )}

                        {messages.length === 1 && !loading && (
                            <div style={styles.suggestionsSection}>
                                <div style={styles.suggestionsTitle}>
                                    Try asking
                                </div>

                                {suggestedQuestions.map((suggestion) => (
                                    <button
                                        key={suggestion}
                                        type="button"
                                        onClick={() => handleAsk(suggestion)}
                                        style={styles.suggestionButton}
                                    >
                                        {suggestion}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    <div style={styles.inputArea}>
                        <textarea
                            value={question}
                            onChange={(event) => setQuestion(event.target.value)}
                            onKeyDown={handleKeyDown}
                            placeholder="Ask BankFlow..."
                            rows={1}
                            disabled={loading}
                            style={styles.input}
                        />

                        <button
                            type="button"
                            onClick={() => handleAsk()}
                            disabled={!question.trim() || loading}
                            style={{
                                ...styles.sendButton,
                                opacity: !question.trim() || loading ? 0.5 : 1,
                                cursor:
                                    !question.trim() || loading
                                        ? "not-allowed"
                                        : "pointer",
                            }}
                        >
                            Send
                        </button>
                    </div>
                </div>
            )}

            {!isOpen && (
                <button
                    type="button"
                    onClick={() => setIsOpen(true)}
                    style={styles.floatingButton}
                    aria-label="Open BankFlow Assistant"
                >
                    <span style={styles.floatingIcon}>✦</span>
                    <span>Ask BankFlow</span>
                </button>
            )}
        </>
    );
};

const styles = {
    floatingButton: {
        position: "fixed",
        right: "28px",
        bottom: "28px",
        zIndex: 1100,
        display: "flex",
        alignItems: "center",
        gap: "8px",
        border: "none",
        borderRadius: "24px",
        padding: "12px 18px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        fontSize: "14px",
        fontWeight: "700",
        cursor: "pointer",
        boxShadow: "0 8px 24px rgba(0, 0, 0, 0.16)",
    },

    floatingIcon: {
        fontSize: "17px",
    },

    chatPanel: {
        position: "fixed",
        right: "28px",
        bottom: "28px",
        width: "390px",
        height: "570px",
        zIndex: 1100,
        display: "flex",
        flexDirection: "column",
        backgroundColor: "#ffffff",
        border: "1px solid #e5e7eb",
        borderRadius: "14px",
        boxShadow: "0 12px 40px rgba(0, 0, 0, 0.16)",
        overflow: "hidden",
    },

    header: {
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        padding: "16px 18px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
    },

    headerTitle: {
        fontSize: "16px",
        fontWeight: "700",
    },

    headerSubtitle: {
        marginTop: "3px",
        fontSize: "11px",
        opacity: 0.85,
    },

    closeButton: {
        border: "none",
        background: "transparent",
        color: "#ffffff",
        fontSize: "26px",
        lineHeight: 1,
        cursor: "pointer",
        padding: "2px 5px",
    },

    messagesContainer: {
        flex: 1,
        overflowY: "auto",
        padding: "18px",
        backgroundColor: "#f8fafc",
    },

    messageRow: {
        display: "flex",
        marginBottom: "12px",
    },

    assistantMessage: {
        maxWidth: "82%",
        padding: "10px 13px",
        borderRadius: "12px 12px 12px 4px",
        backgroundColor: "#ffffff",
        border: "1px solid #e5e7eb",
        color: "#374151",
        fontSize: "13px",
        lineHeight: 1.5,
    },

    userMessage: {
        maxWidth: "82%",
        padding: "10px 13px",
        borderRadius: "12px 12px 4px 12px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        fontSize: "13px",
        lineHeight: 1.5,
    },

    suggestionsSection: {
        marginTop: "18px",
    },

    suggestionsTitle: {
        marginBottom: "8px",
        fontSize: "11px",
        fontWeight: "700",
        color: "#6b7280",
        textTransform: "uppercase",
    },

    suggestionButton: {
        display: "block",
        width: "100%",
        marginBottom: "7px",
        padding: "9px 11px",
        border: "1px solid #dfe5e4",
        borderRadius: "8px",
        backgroundColor: "#ffffff",
        color: "#374151",
        fontSize: "12px",
        textAlign: "left",
        cursor: "pointer",
    },

    inputArea: {
        display: "flex",
        alignItems: "flex-end",
        gap: "8px",
        padding: "12px",
        borderTop: "1px solid #e5e7eb",
        backgroundColor: "#ffffff",
    },

    input: {
        flex: 1,
        resize: "none",
        minHeight: "38px",
        maxHeight: "90px",
        padding: "9px 11px",
        border: "1px solid #d1d5db",
        borderRadius: "8px",
        outline: "none",
        fontFamily: "inherit",
        fontSize: "13px",
        color: "#111827",
    },

    sendButton: {
        border: "none",
        borderRadius: "8px",
        padding: "10px 14px",
        backgroundColor: "#0d6360",
        color: "#ffffff",
        fontSize: "12px",
        fontWeight: "700",
    },
};

export default AIAssistant;