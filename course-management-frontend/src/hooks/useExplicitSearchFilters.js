import { useEffect, useState } from "react";

const readFilters = (searchParams, keys) => Object.fromEntries(keys.map(key => [key, searchParams.get(key) || ""]));

const useExplicitSearchFilters = (searchParams, keys) => {
    const queryString = searchParams.toString();
    const [draft, setDraft] = useState(() => readFilters(searchParams, keys));

    useEffect(() => {
        setDraft(readFilters(searchParams, keys));
    }, [queryString]);

    const setFilter = (name, value, resetKeys = []) => {
        setDraft(prev => {
            const next = { ...prev, [name]: value };
            resetKeys.forEach(key => { next[key] = ""; });
            return next;
        });
    };

    const resetFilters = () => setDraft(Object.fromEntries(keys.map(key => [key, ""])));

    return { draft, setFilter, resetFilters };
};

export default useExplicitSearchFilters;
