import { useEffect, useState } from "react";
import { Form } from "react-bootstrap";
import { authApis } from "../configs/Apis";

const DependentClassSelect = ({
    courseId,
    value,
    onChange,
    endpoint,
    emptyLabel = "Tất cả lớp học",
    required = false,
    disabled = false,
    availableOnly = false
}) => {
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        let active = true;

        const load = async () => {
            if (!courseId) {
                setClasses([]);
                if (value) onChange("");
                return;
            }

            try {
                setLoading(true);
                const params = { courseId };
                if (availableOnly) params.availableOnly = true;
                const res = await authApis().get(endpoint, { params });
                const data = Array.isArray(res.data) ? res.data : [];
                if (!active) return;
                setClasses(data);

                if (value && !data.some(item => String(item.id ?? item.classId) === String(value)))
                    onChange("");
            } catch (ex) {
                if (active) setClasses([]);
            } finally {
                if (active) setLoading(false);
            }
        };

        load();
        return () => { active = false; };
    }, [courseId, endpoint, availableOnly]);

    return (
        <Form.Select value={value} onChange={e => onChange(e.target.value)}
            disabled={disabled || !courseId || loading} required={required}>
            <option value="">
                {!courseId ? "-- Chọn khóa học trước --" : loading ? "Đang tải lớp học..." : emptyLabel}
            </option>
            {classes.map(item => {
                const id = item.id ?? item.classId;
                return <option key={id} value={id}>{item.name ?? item.className}</option>;
            })}
        </Form.Select>
    );
};

export default DependentClassSelect;
