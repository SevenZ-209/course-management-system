import { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { authApis } from "../configs/Apis";

const DependentSessionSelect = ({
    classId,
    value,
    onChange,
    endpoint,
    emptyLabel = "Tất cả buổi học",
    required = false,
    disabled = false
}) => {
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        let active = true;

        const load = async () => {
            if (!classId) {
                setSessions([]);
                return;
            }

            try {
                setLoading(true);
                const res = await authApis().get(endpoint, { params: { classId } });
                const data = Array.isArray(res.data) ? res.data : [];
                if (!active) return;
                setSessions(data);

                if (value && !data.some(item => String(item.id ?? item.sessionId) === String(value)))
                    onChange("");
            } catch (ex) {
                if (active) setSessions([]);
            } finally {
                if (active) setLoading(false);
            }
        };

        load();
        return () => { active = false; };
    }, [classId, endpoint]);

    return (
        <Form.Select value={value} onChange={e => onChange(e.target.value)}
            disabled={disabled || !classId || loading} required={required}>
            <option value="">
                {!classId ? "-- Chọn lớp trước --" : loading ? "Đang tải buổi học..." : emptyLabel}
            </option>
            {sessions.map(item => {
                const id = item.id ?? item.sessionId;
                return <option key={id} value={id}>{item.title ?? item.sessionTitle}</option>;
            })}
        </Form.Select>
    );
};

export default DependentSessionSelect;
