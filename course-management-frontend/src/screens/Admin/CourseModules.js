import { useEffect, useState } from "react";
import { Alert, Button, Card, Form, Modal, Pagination, Table } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { authApis, endpoints } from "../../configs/Apis";
import MySpinner from "../../components/MySpinner";

const CourseModules = () => {
    const nav = useNavigate();

    const [modules, setModules] = useState([]);
    const [courses, setCourses] = useState([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState("");
    const [success, setSuccess] = useState("");
    const [showModal, setShowModal] = useState(false);
    const [editingModule, setEditingModule] = useState(null);
    const [totalRecords, setTotalRecords] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const [page, setPage] = useState(1);

    const [form, setForm] = useState({
        name: "",
        courseId: "",
        orderNumber: ""
    });

    const loadData = async () => {
        try {
            setLoading(true);
            setErr("");

            const [moduleRes, courseRes] = await Promise.all([
                authApis().get(endpoints.adminCourseModules, {
                    params: {
                        page: page - 1
                    }
                }),
                authApis().get(endpoints.adminCourseOptions)
            ]);

            setModules(moduleRes.data.modules || []);
            setTotalRecords(moduleRes.data.totalRecords || 0);
            setTotalPages(moduleRes.data.totalPages || 1);

            setCourses(
                Array.isArray(courseRes.data)
                    ? courseRes.data
                    : []
            );

        } catch(ex) {
            console.error(ex);
            setErr(
                ex.response?.data?.message ||
                "Không thể tải dữ liệu!"
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [page]);

    const openAdd = () => {
        setEditingModule(null);
        setForm({
            name: "",
            courseId: "",
            orderNumber: ""
        });
        setShowModal(true);
    };

    const openEdit = m => {
        setEditingModule(m);
        setForm({
            name: m.name || "",
            courseId: m.courseId ?? m.course?.id ?? "",
            orderNumber: m.orderNumber ?? ""
        });
        setShowModal(true);
    };

    const saveModule = async e => {
        e.preventDefault();

        if(!form.name.trim())
            return setErr("Tên module không được để trống!");

        if(!form.courseId)
            return setErr("Vui lòng chọn khóa học!");

        try {
            setSaving(true);
            setErr("");
            setSuccess("");

            const body = {
                name: form.name.trim(),
                courseId: Number(form.courseId),
                orderNumber: Number(form.orderNumber)
            };

            let res;

            if(editingModule)
                res = await authApis().put(
                    `${endpoints.adminCourseModules}/${editingModule.id}`,
                    body
                );
            else
                res = await authApis().post(
                    endpoints.adminCourseModules,
                    body
                );

            setSuccess(
                res.data?.message ||
                (
                    editingModule
                    ? "Cập nhật module thành công!"
                    : "Thêm module thành công!"
                )
            );

            setShowModal(false);
            setPage(1);

        } catch(ex) {
            console.error(ex);
            setErr(
                ex.response?.data?.message ||
                "Lưu module thất bại!"
            );
        } finally {
            setSaving(false);
        }
    };

    const courseName = m => {
        if(m.courseName)
            return m.courseName;

        const id = m.courseId ?? m.course?.id;

        return courses.find(
            c => String(c.id) === String(id)
        )?.name || "-";
    };

    const changePage = p => {
        setPage(p);
    };

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">

                <div>
                    <h2 className="fw-bold mb-1">
                        Quản lý module khóa học
                    </h2>

                    <p className="text-muted mb-0">
                        Tổng cộng <strong>{totalRecords}</strong> module
                    </p>
                </div>

                <Button onClick={openAdd}>
                    + Thêm module
                </Button>

            </div>

            {success && (
                <Alert
                    variant="success"
                    dismissible
                    onClose={() => setSuccess("")}
                >
                    {success}
                </Alert>
            )}

            {err && (
                <Alert
                    variant="danger"
                    dismissible
                    onClose={() => setErr("")}
                >
                    {err}
                </Alert>
            )}

            <Card className="border-0 shadow-sm">

                <Card.Body className="p-0">

                    {
                        loading ?

                        <div className="text-center p-5">
                            <MySpinner/>
                        </div>

                        :

                        <div className="table-responsive">

                            <Table hover className="align-middle mb-0">

                                <thead className="table-light">

                                    <tr>
                                        <th>ID</th>
                                        <th>Tên module</th>
                                        <th>Khóa học</th>
                                        <th>Thứ tự</th>
                                        <th>Thao tác</th>
                                    </tr>

                                </thead>

                                <tbody>

                                    {
                                        modules.map(m => (

                                            <tr key={m.id}>

                                                <td>
                                                    {m.id}
                                                </td>

                                                <td className="fw-semibold">
                                                    {m.name}
                                                </td>

                                                <td>
                                                    {courseName(m)}
                                                </td>

                                                <td>
                                                    {m.orderNumber}
                                                </td>

                                                <td>

                                                    <Button
                                                        size="sm"
                                                        className="me-2"
                                                        onClick={() =>
                                                            nav(
                                                                `/admin/lessons?moduleId=${m.id}`
                                                            )
                                                        }
                                                    >
                                                        Bài học
                                                    </Button>

                                                    <Button
                                                        size="sm"
                                                        variant="outline-primary"
                                                        onClick={() =>
                                                            openEdit(m)
                                                        }
                                                    >
                                                        Sửa
                                                    </Button>

                                                </td>

                                            </tr>

                                        ))
                                    }

                                </tbody>

                            </Table>

                        </div>
                    }

                </Card.Body>

            </Card>

            {
                totalPages > 1 && (

                    <div className="d-flex justify-content-center mt-4">

                        <Pagination>

                            <Pagination.First
                                disabled={page === 1}
                                onClick={() => changePage(1)}
                            />

                            <Pagination.Prev
                                disabled={page === 1}
                                onClick={() => changePage(page - 1)}
                            />

                            {
                                Array.from(
                                    {length: totalPages},
                                    (_, i) => i + 1
                                ).map(number => (

                                    <Pagination.Item
                                        key={number}
                                        active={page === number}
                                        onClick={() => changePage(number)}
                                    >
                                        {number}
                                    </Pagination.Item>

                                ))
                            }

                            <Pagination.Next
                                disabled={page === totalPages}
                                onClick={() => changePage(page + 1)}
                            />

                            <Pagination.Last
                                disabled={page === totalPages}
                                onClick={() => changePage(totalPages)}
                            />

                        </Pagination>

                    </div>

                )
            }

            <Modal
                show={showModal}
                onHide={() => setShowModal(false)}
                centered
            >

                <Form onSubmit={saveModule}>

                    <Modal.Header closeButton>

                        <Modal.Title>
                            {
                                editingModule
                                ? "Cập nhật module"
                                : "Thêm module"
                            }
                        </Modal.Title>

                    </Modal.Header>

                    <Modal.Body>

                        <Form.Group className="mb-3">

                            <Form.Label>
                                Tên module
                            </Form.Label>

                            <Form.Control
                                value={form.name}
                                onChange={e =>
                                    setForm({
                                        ...form,
                                        name:e.target.value
                                    })
                                }
                            />

                        </Form.Group>

                        <Form.Group className="mb-3">

                            <Form.Label>
                                Khóa học
                            </Form.Label>

                            <Form.Select
                                value={form.courseId}
                                onChange={e =>
                                    setForm({
                                        ...form,
                                        courseId:e.target.value
                                    })
                                }
                            >

                                <option value="">
                                    -- Chọn khóa học --
                                </option>

                                {
                                    courses.map(c => (
                                        <option
                                            key={c.id}
                                            value={c.id}
                                        >
                                            {c.name}
                                        </option>
                                    ))
                                }

                            </Form.Select>

                        </Form.Group>

                        <Form.Group>

                            <Form.Label>
                                Thứ tự module
                            </Form.Label>

                            <Form.Control
                                type="number"
                                min="1"
                                value={form.orderNumber}
                                onChange={e =>
                                    setForm({
                                        ...form,
                                        orderNumber:e.target.value
                                    })
                                }
                            />

                        </Form.Group>

                    </Modal.Body>

                    <Modal.Footer>

                        <Button
                            variant="secondary"
                            onClick={() => setShowModal(false)}
                        >
                            Hủy
                        </Button>

                        <Button
                            type="submit"
                            disabled={saving}
                        >
                            {
                                saving
                                ? "Đang lưu..."
                                : "Lưu"
                            }
                        </Button>

                    </Modal.Footer>

                </Form>

            </Modal>

        </>
    );
};

export default CourseModules;