const PageCard = ({ title, children, action }) => {
    return (
        <div
            style={{
                backgroundColor: "#ffffff",
                border: "1px solid #e2e8f0",
                borderRadius: "12px",
                boxShadow: "0 1px 3px rgba(0,0,0,0.05)",
                padding: "24px",
            }}
        >
            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "20px",
                }}
            >
                <h2
                    style={{
                        margin: 0,
                        fontSize: "22px",
                        color: "#0f172a",
                    }}
                >
                    {title}
                </h2>

                {action}
            </div>

            {children}
        </div>
    );
};

export default PageCard;