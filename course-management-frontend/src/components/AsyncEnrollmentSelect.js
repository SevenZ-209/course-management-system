import { useEffect, useRef, useState } from "react";
import { Form, Spinner } from "react-bootstrap";
import { authApis } from "../configs/Apis";

const AsyncEnrollmentSelect = ({ endpoint, value, onChange, placeholder = "Tìm học viên, lớp hoặc khóa học...", minChars = 2 }) => {
    const [query, setQuery] = useState("");
    const [options, setOptions] = useState([]);
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const wrapperRef = useRef(null);
    const editingRef = useRef(false);

    useEffect(() => {
        if (!value) {
            if (editingRef.current) {
                editingRef.current = false;
                return;
            }
            setQuery("");
            setOptions([]);
            setOpen(false);
        }
    }, [value]);

    useEffect(() => {
        const outside = e => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", outside);
        return () => document.removeEventListener("mousedown", outside);
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
                setOptions([]);
                setOpen(true);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => clearTimeout(timer);
    }, [endpoint, minChars, query, value]);

    const label = option => {
        const student = option.studentName || "Học viên";
        const course = option.courseName ? ` - ${option.courseName}` : "";
        const className = option.className ? ` (${option.className})` : "";
        return `${student}${course}${className}`;
    };

    const choose = option => {
        onChange(option);
        setQuery(label(option));
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
            <Form.Control value={query} onChange={handleInput}
                onFocus={() => options.length && setOpen(true)} placeholder={placeholder}
                autoComplete="off" required />

            {loading && (
                <span className="position-absolute top-50 end-0 translate-middle-y me-3">
                    <Spinner animation="border" size="sm" />
                </span>
            )}

            {open && !loading && (
                <div className="list-group position-absolute w-100 shadow-sm"
                    style={{ zIndex: 1060, maxHeight: 260, overflowY: "auto" }}>
                    {options.length === 0 ? (
                        <div className="list-group-item text-muted">Không tìm thấy đăng ký chờ thanh toán.</div>
                    ) : options.map(option => (
                        <button key={option.enrollmentId} type="button" className="list-group-item list-group-item-action"
                            onClick={() => choose(option)}>
                            <div className="fw-semibold">{option.studentName}</div>
                            <small className="text-muted">{option.courseName} · {option.className} · ID {option.enrollmentId}</small>
                        </button>
                    ))}
                </div>
            )}

        </div>
    );
};

export default AsyncEnrollmentSelect;
