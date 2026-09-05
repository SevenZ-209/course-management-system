import { useEffect, useMemo, useState } from "react";
import { Alert, Badge, Button, Card, Col, Form, Pagination, ProgressBar, Row, Table } from "react-bootstrap";
import { useSearchParams } from "react-router-dom";
import useExplicitSearchFilters from "../../hooks/useExplicitSearchFilters";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const Progress = () => {
    const [q, setQ] = useSearchParams();
    const { draft: draftFilters, setFilter: setDraftFilter, resetFilters: resetDraftFilters } = useExplicitSearchFilters(q, ["courseId", "classId", "status"]);
    const [items, setItems] = useState([]);
    const [courses, setCourses] = useState([]);
    const [classes, setClasses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState("");
    const [kw, setKw] = useState(q.get("kw") || "");
    const [meta, setMeta] = useState({
        totalPages: 1, totalRecords: 0,
        inProgressCount: 0, pausedCount: 0, completedCount: 0, noPathCount: 0
    });

    const page = Number(q.get("page")) || 1;
    const courseId = q.get("courseId") || "";
    const classId = q.get("classId") || "";
    const status = q.get("status") || "";

    useEffect(() => {
        authApis().get(endpoints.managerCourseOptions)
            .then(res => setCourses(Array.isArray(res.data) ? res.data : []))
            .catch(ex => console.error("Load progress course options error:", ex));
    }, []);

    useEffect(() => {
        if (!draftFilters.courseId) {
            setClasses([]);
            return;
        }

        authApis().get(endpoints.managerClassOptions, { params: { courseId: draftFilters.courseId } })
            .then(res => setClasses(Array.isArray(res.data) ? res.data : []))
            .catch(ex => {
                console.error("Load progress class options error:", ex);
                setClasses([]);
            });
    }, [draftFilters.courseId]);

    useEffect(() => {
        const load = async () => {
            try {
                setLoading(true);
                setErr("");
                const params = { page };
                if(q.get("kw")) params.kw = q.get("kw");
                if(courseId) params.courseId = courseId;
                if(classId) params.classId = classId;
                if(status) params.status = status;

                const res = await authApis().get(endpoints.managerProgress, { params });
                const data = res.data || {};
                setItems(data.progress || []);
                setMeta({
                    totalPages: data.totalPages || 1,
                    totalRecords: data.totalRecords || 0,
                    inProgressCount: data.inProgressCount || 0,
                    pausedCount: data.pausedCount || 0,
                    completedCount: data.completedCount || 0,
                    noPathCount: data.noPathCount || 0
                });
            } catch(ex) {
                console.error("Load manager progress error:", ex);
                setErr(ex.response?.data?.message || "Không thể tải tiến độ học tập!");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [q]);

    const filteredClasses = useMemo(() => classes, [classes]);

    const search = e => {
        e.preventDefault();
        const params = { page: "1" };
        if (kw.trim()) params.kw = kw.trim();
        if (draftFilters.courseId) params.courseId = draftFilters.courseId;
        if (draftFilters.classId) params.classId = draftFilters.classId;
        if (draftFilters.status) params.status = draftFilters.status;
        setQ(params);
    };

    const changeFilter = (name, value) => {
        const resetKeys = name === "courseId" ? ["classId"] : [];
        setDraftFilter(name, value, resetKeys);
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

    const statusBadge = value => {
        const config = {
            IN_PROGRESS: ["primary", "Đang học"],
            PAUSED: ["warning", "Tạm dừng"],
            COMPLETED: ["success", "Hoàn thành"]
        }[value] || ["secondary", "Chưa có lộ trình"];
        return <Badge bg={config[0]}>{config[1]}</Badge>;
    };

    const percent = item => {
        const total = Number(item.totalDetails || 0);
        const completed = Number(item.completedDetails || 0);
        if(!total) return 0;
        return Math.min(100, Math.round(completed * 100 / total));
    };

    return (
        <>
            <div className="mb-4">
                <h2 className="fw-bold mb-1">Tiến độ học tập</h2>
                <p className="text-muted mb-0">Theo dõi tiến độ học viên trên toàn hệ thống.</p>
            </div>

            {err && <Alert variant="danger" dismissible onClose={() => setErr("")}>{err}</Alert>}

            <Row className="g-3 mb-4">
                <Col md={3}><Card className="border-0 shadow-sm h-100"><Card.Body><small className="text-muted">Đang học</small><h3 className="mb-0">{meta.inProgressCount}</h3></Card.Body></Card></Col>
                <Col md={3}><Card className="border-0 shadow-sm h-100"><Card.Body><small className="text-muted">Tạm dừng</small><h3 className="mb-0">{meta.pausedCount}</h3></Card.Body></Card></Col>
                <Col md={3}><Card className="border-0 shadow-sm h-100"><Card.Body><small className="text-muted">Hoàn thành</small><h3 className="mb-0">{meta.completedCount}</h3></Card.Body></Card></Col>
                <Col md={3}><Card className="border-0 shadow-sm h-100"><Card.Body><small className="text-muted">Chưa có lộ trình</small><h3 className="mb-0">{meta.noPathCount}</h3></Card.Body></Card></Col>
            </Row>

            <Card className="border-0 shadow-sm mb-4">
                <Card.Body>
                    <Form onSubmit={search}>
                        <Row className="g-3">
                            <Col lg={3}><Form.Control placeholder="Tìm học viên..." value={kw} onChange={e => setKw(e.target.value)} /></Col>
                            <Col lg={2}>
                                <Form.Select value={draftFilters.courseId} onChange={e => changeFilter("courseId", e.target.value)}>
                                    <option value="">Tất cả khóa học</option>
                                    {courses.map(c => <option key={c.id ?? c.courseId} value={c.id ?? c.courseId}>{c.name}</option>)}
                                </Form.Select>
                            </Col>
                            <Col lg={2}>
                                <Form.Select value={draftFilters.classId} onChange={e => changeFilter("classId", e.target.value)}>
                                    <option value="">Tất cả lớp học</option>
                                    {filteredClasses.map(c => <option key={c.id ?? c.classId} value={c.id ?? c.classId}>{c.name}</option>)}
                                </Form.Select>
                            </Col>
                            <Col lg={2}>
                                <Form.Select value={draftFilters.status} onChange={e => changeFilter("status", e.target.value)}>
                                    <option value="">Tất cả tiến độ</option>
                                    <option value="IN_PROGRESS">Đang học</option>
                                    <option value="PAUSED">Tạm dừng</option>
                                    <option value="COMPLETED">Hoàn thành</option>
                                    <option value="NO_PATH">Chưa có lộ trình</option>
                                </Form.Select>
                            </Col>
                            <Col lg={3} className="d-flex gap-2">
                                <Button type="submit">Tìm</Button>
                                <Button variant="outline-secondary" onClick={clearFilters}>Xóa lọc</Button>
                            </Col>
                        </Row>
                    </Form>
                </Card.Body>
            </Card>

            <Card className="border-0 shadow-sm">
                <Card.Body className="p-0">
                    {loading ? <div className="text-center p-5"><MySpinner /></div> : items.length === 0 ? (
                        <Alert variant="info" className="m-4 text-center">Không tìm thấy dữ liệu tiến độ.</Alert>
                    ) : (
                        <div className="table-responsive">
                            <Table hover className="align-middle mb-0">
                                <thead><tr><th>HỌC VIÊN</th><th>KHÓA / LỚP</th><th>GIẢNG VIÊN</th><th>LỘ TRÌNH</th><th>TIẾN ĐỘ</th><th>TRẠNG THÁI</th></tr></thead>
                                <tbody>
                                    {items.map(item => (
                                        <tr key={item.enrollmentId}>
                                            <td><strong>{item.studentName || item.username}</strong><div className="text-muted small">@{item.username}</div></td>
                                            <td><strong>{item.courseName || "-"}</strong><div className="text-muted small">{item.className || "-"}</div></td>
                                            <td>{item.teacherName || "Chưa phân công"}</td>
                                            <td><strong>{item.learningPathName || "Chưa có lộ trình"}</strong><div className="text-muted small">{item.currentAssignmentName ? `Bài hiện tại: ${item.currentAssignmentName}` : "Chưa có bài hiện tại"}</div></td>
                                            <td style={{ minWidth: 180 }}>
                                                <div className="d-flex justify-content-between small mb-1"><span>{item.completedDetails || 0}/{item.totalDetails || 0}</span><span>{percent(item)}%</span></div>
                                                <ProgressBar now={percent(item)} style={{ height: 8 }} />
                                            </td>
                                            <td>{statusBadge(item.learningPathStatus)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </Table>
                        </div>
                    )}
                </Card.Body>
            </Card>

            {meta.totalPages > 1 && (
                <div className="d-flex justify-content-center mt-4">
                    <Pagination className="mb-0">
                        <Pagination.Prev disabled={page <= 1} onClick={() => changePage(page - 1)} />
                        {Array.from({ length: meta.totalPages }, (_, i) => i + 1).map(p => <Pagination.Item key={p} active={p === page} onClick={() => changePage(p)}>{p}</Pagination.Item>)}
                        <Pagination.Next disabled={page >= meta.totalPages} onClick={() => changePage(page + 1)} />
                    </Pagination>
                </div>
            )}
            <div className="text-center text-muted small mt-2">Tổng cộng {meta.totalRecords} học viên phù hợp bộ lọc</div>
        </>
    );
};

export default Progress;
