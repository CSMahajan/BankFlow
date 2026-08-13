import React from "react";

const CardEmptyState = () => {

    return (
        <div style={styles.container}>

            <h3 style={styles.title}>
                No cards found
            </h3>

            <p style={styles.message}>
                Try changing your search or filter criteria.
            </p>

        </div>
    );
};


const styles = {

    container: {
        padding: "40px",
        textAlign: "center",
    },

    title: {
        color: "#334155",
    },

    message: {
        color: "#64748b",
    },

};


export default CardEmptyState;