import { useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Modal, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Categories = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["status"]);

    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const [showModal, setShowModal] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [form, setForm] = useState({ name: "", description: "" });

    const page = Number(q.get("page")) || 1;
    const status = q.get("status") || "";

    const getCategoryId = c => c.id ?? c.categoryId;

    const loadCategories = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminCategories, { params });
            const data = res.data;

            setCategories(data.categories || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load categories error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách danh mục!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadCategories();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.status) params.status = draftFilters.status;

        setQ(params);
    };

    const changeFilter = value => {
        setDraftFilter("status", value);
    };

    const clearFilters = () => {
        setKw("");
        resetDraftFilters();
        setQ({ page: "1" });
    };

    const changePage = newPage => {
        const params = Object.fromEntries(q);
        params.page = String(newPage);
        setQ(params);
    };

    const openAddModal = () => {
        setEditingCategory(null);
        setForm({ name: "", description: "" });
        setShowModal(true);
    };

    const openEditModal = category => {
        setEditingCategory(category);
        setForm({
            name: category.name || "",
            description: category.description || ""
        });
        setShowModal(true);
    };

    const closeModal = () => {
        setShowModal(false);
        setEditingCategory(null);
        setForm({ name: "", description: "" });
    };

    const saveCategory = async e => {
        e.preventDefault();

        if (!form.name.trim())
            return setErr("Tên danh mục không được để trống!");

        try {
            setActionLoading(true);
            setErr("");
            setSuccess("");

            const body = {
                name: form.name.trim(),
                description: form.description.trim()
            };

            let res;

            if (editingCategory) {
                res = await authApis().put(
                    `${endpoints.adminCategories}/${getCategoryId(editingCategory)}`,
                    body
                );
            } else {
                res = await authApis().post(endpoints.adminCategories, body);
            }

            setSuccess(
                res.data?.message ||
                (editingCategory ? "Cập nhật danh mục thành công!" : "Thêm danh mục thành công!")
            );

            closeModal();
            await loadCategories();
        } catch (ex) {
            console.error("Save category error:", ex);
            setErr(ex.response?.data?.message || "Lưu danh mục thất bại!");
        } finally {
            setActionLoading(false);
        }
    };

    const updateStatus = async (categoryId, newStatus) => {
        try {
            setErr("");
            setSuccess("");

            const res = await authApis().patch(
                `${endpoints.adminCategories}/${categoryId}/status`,
                { status: newStatus }
            );

            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadCategories();
        } catch (ex) {
            console.error("Update category status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadCategories();
        }
    };

    const statusBadge = value => {
        if (value === "ACTIVE") return <Badge bg="success">Hoạt động</Badge>;
        return <Badge bg="secondary">Không hoạt động</Badge>;
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2 className="fw-bold mb-1">Quản lý danh mục</h2>
                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> danh mục
                    </p>
                </div>

                <Button onClick={openAddModal}>+ Thêm danh mục</Button>
            </div>

            {success && (
                <Alert variant="success" dismissible onClose={() => setSuccess("")}>
                    {success}
                </Alert>
            )}

            {err && (
                <Alert variant="danger" dismissible onClose={() => setErr("")}>
                    {err}
                </Alert>
            )}

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={7}>
                                <Form.Control placeholder="Tìm theo tên danh mục..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2}>
                                <Form.Select value={draftFilters.status} onChange={e => changeFilter(e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE">Hoạt động</option>
                                    <option value="INACTIVE">Không hoạt động</option>
                                </Form.Select>
                            </Col>

                            <Col lg={3} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? (
                        <div className="text-center p-5"><MySpinner /></div>
                    ) : categories.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">
                            Không tìm thấy danh mục.
                        </Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Tên danh mục</th>
                                        <th>Mô tả</th>
                                        <th>Trạng thái</th>
                                        <th style={{ width: 150 }}>Cập nhật trạng thái</th>
                                        <th style={{ width: 100 }}>Thao tác</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {categories.map(category => {
                                        const id = getCategoryId(category);

                                        return (
                                            <tr key={id}>
                                                <td>{id}</td>
                                                <td className="fw-semibold">{category.name}</td>
                                                <td>{category.description || "-"}</td>
                                                <td>{statusBadge(category.status)}</td>

                                                <td>
                                                    <Form.Select size="sm" value={category.status}
                                                        onChange={e => updateStatus(id, e.target.value)}>
                                                        <option value="ACTIVE">Hoạt động</option>
                                                        <option value="INACTIVE">Không hoạt động</option>
                                                    </Form.Select>
                                                </td>

                                                <td>
                                                    <Button size="sm" variant="outline-primary"
                                                        onClick={() => openEditModal(category)}>
                                                        Sửa
                                                    </Button>
                                                </td>
                                            </tr>
                                        );
                                    })}
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
                        <Pagination.Prev disabled={page === 1}
                            onClick={() => changePage(Math.max(page - 1, 1))} />

                        {Array.from({ length: totalPages }, (_, i) => i + 1).map(number => (
                            <Pagination.Item key={number} active={number === page}
                                onClick={() => changePage(number)}>
                                {number}
                            </Pagination.Item>
                        ))}

                        <Pagination.Next disabled={page === totalPages}
                            onClick={() => changePage(Math.min(page + 1, totalPages))} />
                        <Pagination.Last disabled={page === totalPages}
                            onClick={() => changePage(totalPages)} />
                    </Pagination>
                </div>
            )}

            <Modal show={showModal} onHide={closeModal} centered>
                <Form onSubmit={saveCategory}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {editingCategory ? "Cập nhật danh mục" : "Thêm danh mục"}
                        </Modal.Title>
                    </Modal.Header>

                    <Modal.Body>
                        <Form.Group className="mb-3">
                            <Form.Label>Tên danh mục</Form.Label>
                            <Form.Control value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required />
                        </Form.Group>

                        <Form.Group>
                            <Form.Label>Mô tả</Form.Label>
                            <Form.Control as="textarea" rows={4} value={form.description}
                                onChange={e => setForm({ ...form, description: e.target.value })} />
                        </Form.Group>
                    </Modal.Body>

                    <Modal.Footer>
                        <Button variant="secondary" onClick={closeModal} disabled={actionLoading}>
                            Hủy
                        </Button>

                        <Button type="submit" disabled={actionLoading}>
                            {actionLoading ? "Đang lưu..." : "Lưu"}
                        </Button>
                    </Modal.Footer>
                </Form>
            </Modal>
        </>
    );
};

export default Categories;
