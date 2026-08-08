export const getActiveAccounts = (accounts = []) =>
    accounts.filter(account => account.accountStatus === "ACTIVE");

export const getSelectedAccount = (
    activeAccounts,
    accountNumber
) =>
    activeAccounts.find(
        account => account.accountNumber === accountNumber
    );