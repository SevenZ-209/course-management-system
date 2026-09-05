import { useEffect, useRef, useState } from "react";
import { Form, Spinner } from "react-bootstrap";
import { authApis } from "../configs/Apis";

const AsyncUserSelect = ({ endpoint, value, onChange, placeholder = "Tìm tên, username hoặc email...", minChars = 2, required = false }) => {
    const [query, setQuery] = useState("");
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const wrapperRef = useRef(null);
    const editingRef = useRef(false);

    useEffect(() => {
        let active = true;

        const hydrate = async () => {
            if (!value) {
                if (editingRef.current) {
                    editingRef.current = false;
                    return;
                }
                setQuery("");
                setOptions([]);
                setOpen(false);
                return;
            }

            if (query.trim()) return;

            try {
                const res = await authApis().get(`${endpoint}/${value}`);
                if (!active) return;
                const option = res.data;
                if (option?.id)
                    setQuery(`${option.fullName || option.username} (@${option.username})`);
            } catch (ex) {
                if (active) setQuery(`ID ${value}`);
            }
        };

        hydrate();
        return () => { active = false; };
    }, [endpoint, value]);

    useEffect(() => {
        const handleOutside = e => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", handleOutside);
        return () => document.removeEventListener("mousedown", handleOutside);
    }, []);

    useEffect(() => {
        const keyword = query.trim();
        if (value || keyword.length < minChars) {
            if (!value) setOptions([]);
            return;
        }

        const timer = setTimeout(async () => {
            try {
                setLoading(true);
                const res = await authApis().get(endpoint, { params: { q: keyword, page: 1, size: 20 } });
                setOptions(Array.isArray(res.data) ? res.data : []);
                setOpen(true);
            } catch (ex) {
                console.error("Async user lookup error:", ex);
                setOptions([]);
                setOpen(true);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [endpoint, minChars, query, value]);

    const selectOption = option => {
        onChange(option);
        setQuery(`${option.fullName || option.username} (@${option.username})`);
        setOptions([]);
        setOpen(false);
    };

    const handleInput = e => {
        if (value) {
            editingRef.current = true;
            onChange(null);
        }
        setQuery(e.target.value);
        if (e.target.value.trim().length >= minChars) setOpen(true);
    };

    return (
        <div className="position-relative" ref={wrapperRef}>
            <Form.Control value={query} onChange={handleInput} onFocus={() => options.length && setOpen(true)}
                placeholder={placeholder} autoComplete="off" required={required} />

            {loading && (
                <span className="position-absolute top-50 end-0 translate-middle-y me-3">
                    <Spinner animation="border" size="sm" />
                </span>
            )}

            {open && !loading && (
                <div className="list-group position-absolute w-100 shadow-sm"
                    style={{ zIndex: 1060, maxHeight: 240, overflowY: "auto" }}>
                    {options.length === 0 ? (
                        <div className="list-group-item text-muted">Không tìm thấy người dùng.</div>
                    ) : options.map(option => (
                        <button key={option.id} type="button" className="list-group-item list-group-item-action"
                            onClick={() => selectOption(option)}>
                            <div className="fw-semibold">{option.fullName || option.username}</div>
                            <small className="text-muted">@{option.username} · ID {option.id}</small>
                        </button>
                    ))}
                </div>
            )}

        </div>
    );
};

export default AsyncUserSelect;
