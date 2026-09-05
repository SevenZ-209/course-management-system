import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import AsyncUserSelect from "../../components/AsyncUserSelect";
import MySpinner from "../../components/MySpinner";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";

const localDateTimeValue = date => {
    const value = new Date(date);
    value.setMinutes(value.getMinutes() - value.getTimezoneOffset());
    return value.toISOString().slice(0, 16);
};

const ParentLinks = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters } = useExplicitSearchFilters(q, ["studentId", "parentId", "status"]);

    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");
    const [showModal, setShowModal] = useState(false);
    const [form, setForm] = useState({ studentId: "", expiresAt: localDateTimeValue(Date.now() + 24 * 60 * 60 * 1000) });

    const page = Number(q.get("page")) || 1;
    const studentId = q.get("studentId") || "";
    const parentId = q.get("parentId") || "";
    const status = q.get("status") || "";

    const loadItems = async () => {
        try {
            setLoading(true);
            setErr("");
            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (studentId) params.studentId = studentId;
            if (parentId) params.parentId = parentId;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminParentLinks, { params });
            const data = res.data || {};
            setItems(data.parentLinks || data.items || data.content || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load parent links error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải liên kết phụ huynh!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadItems(); }, [q]);

    const search = e => {
        e.preventDefault();
        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.studentId) params.studentId = draftFilters.studentId;
        if (draftFilters.parentId) params.parentId = draftFilters.parentId;
        if (draftFilters.status) params.status = draftFilters.status;
        setQ(params);
    };

    const clearFilters = () => {
        setKw("");
        resetFilters();
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const openCreate = () => {
        setErr("");
        setForm({ studentId: "", expiresAt: localDateTimeValue(Date.now() + 24 * 60 * 60 * 1000) });
        setShowModal(true);
    };

    const closeCreate = () => {
        setShowModal(false);
        setForm({ studentId: "", expiresAt: localDateTimeValue(Date.now() + 24 * 60 * 60 * 1000) });
    };

    const createLink = async e => {
        e.preventDefault();
        if (!form.studentId) return setErr("Vui lòng chọn học viên!");
        if (!form.expiresAt) return setErr("Vui lòng chọn thời gian hết hạn!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");
            const res = await authApis().post(endpoints.adminParentLinks, {
                studentId: Number(form.studentId),
                expiresAt: form.expiresAt
            });
            setSuccess(res.data?.message || "Tạo mã liên kết thành công!");
            closeCreate();
            await loadItems();
        } catch (ex) {
            console.error("Create parent link error:", ex);
            setErr(ex.response?.data?.message || "Tạo mã liên kết thất bại!");
        } finally {
            setSaving(false);
        }
    };

    const expireLink = async id => {
        if (!window.confirm("Bạn có chắc muốn vô hiệu hóa mã liên kết này?")) return;

        try {
            setErr("");
            setSuccess("");
            const res = await authApis().patch(`${endpoints.adminParentLinks}/${id}/expire`);
            setSuccess(res.data?.message || "Đã vô hiệu hóa mã liên kết!");
            await loadItems();
        } catch (ex) {
            console.error("Expire parent link error:", ex);
            setErr(ex.response?.data?.message || "Không thể vô hiệu hóa mã liên kết!");
        }
    };

    const statusBadge = value => {
        const config = {
            UNUSED: ["warning", "Chưa sử dụng"],
            USED: ["success", "Đã liên kết"],
            EXPIRED: ["secondary", "Hết hạn"],
            UNLINKED: ["danger", "Đã hủy liên kết"]
        }[value] || ["secondary", value || "-"];
        return <Badge bg={config[0]} text={value === "UNUSED" ? "dark" : undefined}>{config[1]}</Badge>;
    };

    const formatDateTime = value => value ? new Date(value).toLocaleString("vi-VN") : "-";

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Liên kết phụ huynh</h2>
                    <p className="text-muted mb-0">Tổng cộng <strong>{totalRecords}</strong> mã liên kết</p>
                </div>
                <Button onClick={openCreate}>+ Tạo mã liên kết</Button>
            </div>

            {success && <Alert variant="success" dismissible onClose={() => setSuccess("")}>{success}</Alert>}
            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}

            <Card className="border-0 shadow-sm mb-4" style={{ overflow: "visible", position: "relative", zIndex: 2 }}>
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col xl={3} lg={6}>
                                <Form.Control value={kw} onChange={e => setKw(e.target.value)} placeholder="Mã, phụ huynh hoặc học viên..." />
                            </Col>
                            <Col xl={2} lg={6}>
                                <AsyncUserSelect endpoint={endpoints.adminStudentOptions} value={draftFilters.studentId}
                                    onChange={user => setDraftFilter("studentId", user?.id || "")} placeholder="Tìm học viên..." />
                            </Col>
                            <Col xl={2} lg={6}>
                                <AsyncUserSelect endpoint={endpoints.adminParentOptions} value={draftFilters.parentId}
                                    onChange={user => setDraftFilter("parentId", user?.id || "")} placeholder="Tìm phụ huynh..." />
                            </Col>
                            <Col xl={2} lg={6}>
                                <Form.Select value={draftFilters.status} onChange={e => setDraftFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="UNUSED">Chưa sử dụng</option>
                                    <option value="USED">Đã liên kết</option>
                                    <option value="EXPIRED">Hết hạn</option>
                                    <option value="UNLINKED">Đã hủy liên kết</option>
                                </Form.Select>
                            </Col>
                            <Col xl={3} className="d-flex gap-2 align-items-start">
                                <Button type="submit" className="flex-grow-1">Tìm</Button>
                                <Button type="button" variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? (
                        <div className="text-center p-5"><MySpinner /></div>
                    ) : items.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">Không tìm thấy mã liên kết.</Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th><th>Mã liên kết</th><th>Học viên</th><th>Phụ huynh</th>
                                        <th>Ngày tạo</th><th>Hết hạn</th><th>Trạng thái</th><th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {items.map(item => (
                                        <tr key={item.id}>
                                            <td>{item.id}</td>
                                            <td className="fw-semibold">{item.verificationCode}</td>
                                            <td>
                                                <div className="fw-semibold">{item.studentName || item.studentUsername}</div>
                                                <small className="text-muted">@{item.studentUsername}</small>
                                            </td>
                                            <td>
                                                {item.parentId ? (
                                                    <><div className="fw-semibold">{item.parentName || item.parentUsername}</div><small className="text-muted">@{item.parentUsername}</small></>
                                                ) : <span className="text-muted">Chưa liên kết</span>}
                                            </td>
                                            <td>{formatDateTime(item.createdAt)}</td>
                                            <td>{formatDateTime(item.expiresAt)}</td>
                                            <td>{statusBadge(item.status)}</td>
                                            <td>
                                                {item.status === "UNUSED" ? (
                                                    <Button size="sm" variant="outline-danger" onClick={() => expireLink(item.id)}>Vô hiệu hóa</Button>
                                                ) : <span className="text-muted">-</span>}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            {!loading && totalPages > 1 && (
                <div className="d-flex justify-content-center mt-4">
                    <Pagination>
                        <Pagination.First disabled={page === 1} onClick={() => changePage(1)} />
                        <Pagination.Prev disabled={page === 1} onClick={() => changePage(Math.max(page - 1, 1))} />
                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                            <Pagination.Item key={n} active={n === page} onClick={() => changePage(n)}>{n}</Pagination.Item>
                        ))}
                        <Pagination.Next disabled={page === totalPages} onClick={() => changePage(Math.min(page + 1, totalPages))} />
                        <Pagination.Last disabled={page === totalPages} onClick={() => changePage(totalPages)} />
                    </Pagination>
                </div>
            )}

            <Modal show={showModal} onHide={closeCreate} centered>
                <Form onSubmit={createLink}>
                    <Modal.Header closeButton><Modal.Title>Tạo mã liên kết</Modal.Title></Modal.Header>
                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Học viên</Form.Label>
                            <AsyncUserSelect endpoint={endpoints.adminStudentOptions} value={form.studentId}
                                onChange={user => setForm(prev => ({ ...prev, studentId: user?.id || "" }))}
                                placeholder="Tìm tên, username hoặc email..." required />
                        </Form.Group>
                        <Form.Group className="mb-3">
                            <Form.Label>Thời gian hết hạn</Form.Label>
                            <Form.Control type="datetime-local" min={localDateTimeValue(Date.now())} value={form.expiresAt}
                                onChange={e => setForm(prev => ({ ...prev, expiresAt: e.target.value }))} required />
                        </Form.Group>
                        <Alert variant="info" className="mb-0">
                            Một học viên có thể liên kết với nhiều phụ huynh; mã này chỉ dùng một lần và chỉ có hiệu lực tới thời gian đã chọn.
                        </Alert>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeCreate} disabled={saving}>Hủy</Button>
                        <Button type="submit" disabled={saving}>{saving ? "Đang tạo..." : "Tạo mã"}</Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default ParentLinks;
