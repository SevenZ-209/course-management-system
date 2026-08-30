import { useRef, useState } from "react";
import { Alert, Button, Form, Container, Card } from "react-bootstrap";
import { Link, useNavigate, useSearchParams } from "react-router-dom";

import MySpinner from "../../components/MySpinner";
import Apis, { endpoints } from "../../configs/Apis";

const Register = () => {
    const userInfo = [
        { field: "fullName", label: "Họ và tên", type: "text" },
        { field: "email", label: "Email", type: "email" },
        { field: "username", label: "Tên đăng nhập", type: "text" },
        { field: "password", label: "Mật khẩu", type: "password" },
        { field: "confirm", label: "Xác nhận mật khẩu", type: "password" }
    ];

    const [user, setUser] = useState({ role: "STUDENT" });
    const [err, setErr] = useState("");
    const [loading, setLoading] = useState(false);

    const avatar = useRef();
    const nav = useNavigate();
    const [q] = useSearchParams();

    const validate = () => {
        if(!user.fullName?.trim()) {
            setErr("Vui lòng nhập họ và tên!");
            return false;
        }
    
        if(!user.username || user.username.length < 5) {
            setErr("Tên đăng nhập phải từ 5 ký tự trở lên!");
            return false;
        }
    
        const emailRegex = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/;
    
        if(!user.email || !emailRegex.test(user.email)) {
            setErr("Email không đúng định dạng!");
            return false;
        }
    
        if(!user.password || user.password.length < 8) {
            setErr("Mật khẩu phải từ 8 ký tự trở lên!");
            return false;
        }
    
        if(!/[0-9]/.test(user.password)) {
            setErr("Mật khẩu phải có số!");
            return false;
        }
    
        if(!/[a-z]/.test(user.password)) {
            setErr("Mật khẩu phải có ký tự thường!");
            return false;
        }
    
        if(!/[A-Z]/.test(user.password)) {
            setErr("Mật khẩu phải có ký tự hoa!");
            return false;
        }
    
        if(user.password !== user.confirm) {
            setErr("Mật khẩu xác nhận không khớp!");
            return false;
        }
    
        const file = avatar.current?.files[0];
    
        if(!file) {
            setErr("Vui lòng chọn ảnh đại diện!");
            return false;
        }
    
        if(!["image/jpeg", "image/png", "image/gif"].includes(file.type)) {
            setErr("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc GIF!");
            return false;
        }
    
        if(file.size > 2 * 1024 * 1024) {
            setErr("Dung lượng ảnh đại diện không được vượt quá 2MB!");
            return false;
        }
    
        setErr("");
        return true;
    };

    const register = async e => {
        e.preventDefault();

        if(!validate()) return;

        const form = new FormData();

        Object.entries({
            fullName: user.fullName.trim(),
            email: user.email.trim(),
            username: user.username.trim(),
            password: user.password,
            role: user.role
        }).forEach(([key, value]) => form.append(key, value));

        form.append("avatar", avatar.current.files[0]);

        try {
            setLoading(true);
            setErr("");

            const res = await Apis.post(endpoints.register, form);

            if(res.status === 201) {
                alert("Đăng ký tài khoản thành công!");

                const next = q.get("next");
                nav(next ? `/login?next=${encodeURIComponent(next)}` : "/login");
            }

        } catch(ex) {
            console.error(ex);
            setErr(ex.response?.data?.message || "Đăng ký thất bại!");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container className="d-flex justify-content-center align-items-center mt-5 mb-5">
            <Card className="shadow-lg p-4 p-md-5 border-0 rounded-4" style={{ maxWidth: "600px", width: "100%" }}>
                <div className="text-center mb-4">
                    <h2 className="text-primary fw-bold">TẠO TÀI KHOẢN</h2>
                    <p className="text-muted">Course Management System</p>
                </div>

                {err && <Alert variant="danger" className="rounded-3">{err}</Alert>}

                <Form onSubmit={register}>
                    <div className="row">
                        {userInfo.map(u => (
                            <Form.Group key={u.field} className="mb-3 col-12 col-md-6">
                                <Form.Label className="fw-semibold">{u.label}</Form.Label>
                                <Form.Control
                                    type={u.type}
                                    placeholder={`Nhập ${u.label.toLowerCase()}`}
                                    value={user[u.field] || ""}
                                    onChange={e => setUser({...user, [u.field]: e.target.value})}
                                    disabled={loading}
                                    required
                                    className="rounded-3"
                                />
                            </Form.Group>
                        ))}

                        <Form.Group className="mb-3 col-12">
                            <Form.Label className="fw-semibold">Bạn đăng ký với vai trò</Form.Label>
                            <Form.Select
                                value={user.role}
                                onChange={e => setUser({...user, role: e.target.value})}
                                disabled={loading}
                                className="rounded-3"
                            >
                                <option value="STUDENT">Học viên</option>
                                <option value="PARENT">Phụ huynh</option>
                            </Form.Select>
                        </Form.Group>
                    </div>

                    <Form.Group className="mb-4">
                        <Form.Label className="fw-semibold">
                            Ảnh đại diện <span className="text-danger">*</span>
                        </Form.Label>

                        <Form.Control
                            ref={avatar}
                            type="file"
                            accept="image/jpeg,image/png,image/gif"
                            disabled={loading}
                            required
                            className="rounded-3"
                        />

                        <Form.Text className="text-muted">
                            JPG, JPEG, PNG hoặc GIF. Tối đa 2MB.
                        </Form.Text>
                    </Form.Group>

                    {loading ?
                        <div className="text-center"><MySpinner /></div>
                        :
                        <Button variant="primary" type="submit" className="w-100 rounded-pill py-2 fw-bold">
                            Đăng ký
                        </Button>
                    }
                </Form>

                <div className="text-center mt-4">
                    <span className="text-muted">Đã có tài khoản? </span>
                    <Link
                        to={q.get("next") ? `/login?next=${encodeURIComponent(q.get("next"))}` : "/login"}
                        className="fw-bold text-decoration-none"
                    >
                        Đăng nhập
                    </Link>
                </div>
            </Card>
        </Container>
    );
};

export default Register;