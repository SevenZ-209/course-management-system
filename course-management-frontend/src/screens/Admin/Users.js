import { useContext, useEffect, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Pagination, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import { MyUserContext } from "../../configs/Contexts";
import MySpinner from "../../components/MySpinner";

const Users = () => {
    const [currentUser] = useContext(MyUserContext);
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["role", "status"]);

    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState(null);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);
    const [kw, setKw] = useState(q.get("kw") || "");

    const page = Number(q.get("page")) || 1;
    const role = q.get("role") || "";
    const status = q.get("status") || "";

    const loadUsers = async () => {
        try {
            setLoading(true);
            setErr("");

            const params = { page };
            if (q.get("kw")) params.kw = q.get("kw");
            if (role) params.role = role;
            if (status) params.status = status;

            const res = await authApis().get(endpoints.adminUsers, { params });
            const data = res.data;

            setUsers(data.users || data.content || data.items || []);
            setTotalPages(data.totalPages || 1);
            setTotalRecords(data.totalRecords || 0);
        } catch (ex) {
            console.error("Load users error:", ex);
            setErr(ex.response?.data?.message || "Không thể tải danh sách người dùng!");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadUsers();
    }, [q]);

    const search = e => {
        e.preventDefault();

        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.role) params.role = draftFilters.role;
        if (draftFilters.status) params.status = draftFilters.status;

        setQ(params);
    };

    const changeFilter = (name, value) => {
        setDraftFilter(name, value);
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

    const updateRole = async (userId, newRole) => {
        try {
            setActionLoading(`role-${userId}`);
            setErr("");
            setSuccess("");

            const res = await authApis().patch(`${endpoints.adminUsers}/${userId}/role`, { role: newRole });
            setSuccess(res.data?.message || "Cập nhật quyền thành công!");
            await loadUsers();
        } catch (ex) {
            console.error("Update role error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật quyền thất bại!");
            await loadUsers();
        } finally {
            setActionLoading(null);
        }
    };

    const updateStatus = async (userId, newStatus) => {
        try {
            setActionLoading(`status-${userId}`);
            setErr("");
            setSuccess("");

            const res = await authApis().patch(`${endpoints.adminUsers}/${userId}/status`, { status: newStatus });
            setSuccess(res.data?.message || "Cập nhật trạng thái thành công!");
            await loadUsers();
        } catch (ex) {
            console.error("Update status error:", ex);
            setErr(ex.response?.data?.message || "Cập nhật trạng thái thất bại!");
            await loadUsers();
        } finally {
            setActionLoading(null);
        }
    };

    const getUserId = u => u.id ?? u.userId;

    const roleName = value => ({
        ADMIN: "Quản trị viên",
        MANAGER: "Quản lý",
        TEACHER: "Giáo viên",
        STUDENT: "Học viên",
        PARENT: "Phụ huynh"
    }[value] || value);

    const statusBadge = value => {
        const config = {
            ACTIVE: ["success", "Hoạt động"],
            INACTIVE: ["secondary", "Không hoạt động"],
            LOCKED: ["danger", "Đã khóa"]
        }[value] || ["secondary", value || "-"];

        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    return (
        <>
            <div className="mb-4">
                <h2 className="fw-bold mb-1">Quản lý người dùng</h2>
                <p className="text-muted mb-0">Tổng cộng <strong>{totalRecords}</strong> người dùng</p>
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
                            <Col lg={5}>
                                <Form.Control type="text" placeholder="Tìm theo tên, username hoặc email..."
                                    value={kw} onChange={e => setKw(e.target.value)} />
                            </Col>

                            <Col lg={2} md={4}>
                                <Form.Select value={draftFilters.role} onChange={e => changeFilter("role", e.target.value)}>
                                    <option value="">Tất cả vai trò</option>
                                    <option value="ADMIN">Quản trị viên</option>
                                    <option value="MANAGER">Quản lý</option>
                                    <option value="TEACHER">Giáo viên</option>
                                    <option value="STUDENT">Học viên</option>
                                    <option value="PARENT">Phụ huynh</option>
                                </Form.Select>
                            </Col>

                            <Col lg={2} md={4}>
                                <Form.Select value={draftFilters.status} onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả trạng thái</option>
                                    <option value="ACTIVE">Hoạt động</option>
                                    <option value="INACTIVE">Không hoạt động</option>
                                    <option value="LOCKED">Đã khóa</option>
                                </Form.Select>
                            </Col>

                            <Col lg={3} md={4} className="d-flex gap-2">
                                <Button type="submit" className="flex-grow-1">Tìm kiếm</Button>
                                <Button type="button" variant="outline-secondary" onClick={clearFilters}>
                                    Xóa lọc
                                </Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? (
                        <div className="text-center p-5"><MySpinner /></div>
                    ) : users.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">Không tìm thấy người dùng.</Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead className="table-light">
                                    <tr>
                                        <th>ID</th>
                                        <th>Người dùng</th>
                                        <th>Email</th>
                                        <th>Vai trò</th>
                                        <th>Trạng thái</th>
                                        <th>Cập nhật role</th>
                                        <th>Cập nhật trạng thái</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {users.map(u => {
                                        const userId = getUserId(u);
                                        const isCurrentUser = userId === (currentUser?.id ?? currentUser?.userId);

                                        return (
                                            <tr key={userId}>
                                                <td>{userId}</td>

                                                <td>
                                                    <div className="fw-semibold">
                                                        {u.fullName}
                                                        {isCurrentUser && <Badge bg="primary" className="ms-2">Bạn</Badge>}
                                                    </div>
                                                    <small className="text-muted">@{u.username}</small>
                                                </td>

                                                <td>{u.email}</td>

                                                <td>
                                                    <Badge bg="info" text="dark">{roleName(u.role)}</Badge>
                                                </td>

                                                <td>{statusBadge(u.status)}</td>

                                                <td style={{ minWidth: 140 }}>
                                                    <Form.Select size="sm" value={u.role}
                                                        disabled={isCurrentUser || actionLoading === `role-${userId}`}
                                                        onChange={e => updateRole(userId, e.target.value)}>
                                                        <option value="ADMIN">Admin</option>
                                                        <option value="MANAGER">Manager</option>
                                                        <option value="TEACHER">Teacher</option>
                                                        <option value="STUDENT">Student</option>
                                                        <option value="PARENT">Parent</option>
                                                    </Form.Select>
                                                </td>

                                                <td style={{ minWidth: 160 }}>
                                                    <Form.Select size="sm" value={u.status}
                                                        disabled={isCurrentUser || actionLoading === `status-${userId}`}
                                                        onChange={e => updateStatus(userId, e.target.value)}>
                                                        <option value="ACTIVE">Active</option>
                                                        <option value="INACTIVE">Inactive</option>
                                                        <option value="LOCKED">Locked</option>
                                                    </Form.Select>
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
        </>
    );
};

export default Users;
