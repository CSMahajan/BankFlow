import { useEffect, useState } from "react";
import {
    fetchAllAccounts,
    fetchAccountSummary,
    fetchAccountTransactions,
    freezeAccount,
    unfreezeAccount
} from "../api/bankService";
import toast from "react-hot-toast";


const useAccounts = ({
    refreshDashboard
}) => {

    const [accounts, setAccounts] = useState([]);
    const [pageData, setPageData] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);

    const [loading, setLoading] = useState(true);

    const [search, setSearch] = useState("");
    const [accountStatusFilter, setAccountStatusFilter] = useState("ALL");

    const [accountSummary, setAccountSummary] = useState(null);

    const [accountTransactions, setAccountTransactions] = useState({});
    const [transactionLoading, setTransactionLoading] = useState(false);

    const [debouncedSearch, setDebouncedSearch] = useState(search);

    useEffect(() => {

        const timer = setTimeout(() => {
            setDebouncedSearch(search);
        }, 500);

        return () => clearTimeout(timer);

    }, [search]);


    const loadAccounts = async () => {

        try {

            setLoading(true);

            const response = await fetchAllAccounts({
                page: currentPage,
                size: 20,
                search: debouncedSearch,
                status: accountStatusFilter
            });


            setAccounts(response.content);
            setPageData(response);

        }
        catch (err) {

            console.error(err);
            toast.error("Unable to load accounts.");

        }
        finally {
            setLoading(false);
        }
    };


    const loadSummary = async () => {

        try {

            const response = await fetchAccountSummary();

            setAccountSummary(response);

        }
        catch (err) {
            console.error(err);
        }
    };


    useEffect(() => {
        loadAccounts();
    }, [
        currentPage,
        debouncedSearch,
        accountStatusFilter
    ]);


    useEffect(() => {
        loadSummary();
    }, []);



    const loadAccountTransactions = async (
        accountNumber,
        page = 0
    ) => {

        try {

            setTransactionLoading(true);

            const response =
                await fetchAccountTransactions(
                    accountNumber,
                    page,
                    10
                );


            setAccountTransactions(prev => ({
                ...prev,
                [accountNumber]: response
            }));

        }
        finally {
            setTransactionLoading(false);
        }

    };


    const handleToggleStatus = async (account) => {

        try {

            const response =
                account.accountStatus === "ACTIVE"
                    ? await freezeAccount(account.accountNumber)
                    : await unfreezeAccount(account.accountNumber);


            await loadAccounts();
            await loadSummary();
            await refreshDashboard?.();


            toast.success(
                response.accountStatus === "ACTIVE"
                    ? "Account unfrozen successfully."
                    : "Account frozen successfully."
            );


            return true;

        }
        catch (err) {

            toast.error(
                "Failed to update account status."
            );

            return false;
        }

    };


    return {
        accounts,
        pageData,
        currentPage,
        setCurrentPage,

        loading,

        search,
        setSearch,

        accountStatusFilter,
        setAccountStatusFilter,

        accountSummary,

        accountTransactions,
        transactionLoading,
        loadAccountTransactions,

        handleToggleStatus
    };

};

export default useAccounts;