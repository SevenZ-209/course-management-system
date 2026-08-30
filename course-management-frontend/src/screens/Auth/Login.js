import { useContext, useState } from "react";
import { Alert, Button, Form, Container, Card } from "react-bootstrap";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import cookies from "react-cookies";

import MySpinner from "../../components/MySpinner";
import Apis, { endpoints } from "../../configs/Apis";
import { MyUserContext } from "../../configs/Contexts";

const Login = () => {
    const userInfo = [
        { field: "username", label: "Tên đăng nhập", type: "text" },
        { field: "password", label: "Mật khẩu", type: "password" }
    ];

    const [user, setUser] = useState({
        username: "",
        password: ""
    });

    const [err, setErr] = useState("");
    const [loading, setLoading] = useState(false);

    const [, dispatch] = useContext(MyUserContext);
    const [q] = useSearchParams();
    const nav = useNavigate();


    const validate = () => {
        if(!user.username.trim()) {
            setErr("Vui lòng nhập tên đăng nhập!");
            return false;
        }

        if(!user.password) {
            setErr("Vui lòng nhập mật khẩu!");
            return false;
        }

        setErr("");
        return true;
    };


    const login = async e => {
        e.preventDefault();

        if(!validate())
            return;

        try {
            setLoading(true);
            setErr("");

            const res = await Apis.post(endpoints.login, {
                username: user.username.trim(),
                password: user.password
            });

            const data = res.data;

            const currentUser = {
                userId: data.userId,
                username: data.username,
                fullName: data.fullName,
                role: data.role?.toUpperCase()
            };

            cookies.save("token", data.token, { path: "/" });
            cookies.save("user", currentUser, { path: "/" });

            dispatch({
                type: "LOGIN",
                payload: currentUser
            });


            const next = q.get("next");

            if(next) {
                const isPublicNext =
                    next.startsWith("/courses") ||
                    next === "/";
            
                const isRoleNext =
                    (currentUser.role === "STUDENT" && next.startsWith("/student")) ||
                    (currentUser.role === "TEACHER" && next.startsWith("/teacher")) ||
                    (currentUser.role === "ADMIN" && next.startsWith("/admin")) ||
                    (currentUser.role === "PARENT" && next.startsWith("/parent"));
            
                if(isPublicNext || isRoleNext) {
                    nav(next);
                    return;
                }
            }


            switch(currentUser.role) {
                case "ADMIN":
                    nav("/admin");
                    break;
            
                case "STUDENT":
                    nav("/student");
                    break;
            
                case "TEACHER":
                    nav("/teacher");
                    break;
            
                case "PARENT":
                    nav("/parent");
                    break;
            
                default:
                    nav("/");
            }

        } catch(ex) {
            console.error("Login error:", ex);

            setErr(
                ex.response?.data?.message ||
                "Đăng nhập thất bại! Vui lòng kiểm tra lại thông tin."
            );

        } finally {
            setLoading(false);
        }
    };


    return (
        <Container className="d-flex justify-content-center align-items-center mt-5 mb-5">
            <Card
                className="shadow-lg p-4 p-md-5 border-0 rounded-4"
                style={{ maxWidth: "450px", width: "100%" }}
            >
                <div className="text-center mb-4">
                    <h2 className="text-primary fw-bold">
                        ĐĂNG NHẬP
                    </h2>

                    <p className="text-muted mb-0">
                        Hệ thống quản lý khóa học
                    </p>
                </div>


                {err && (
                    <Alert variant="danger" className="rounded-3">
                        {err}
                    </Alert>
                )}


                <Form onSubmit={login}>
                    {userInfo.map(u => (
                        <Form.Group key={u.field} className="mb-4">
                            <Form.Label className="fw-semibold text-dark">
                                {u.label}
                            </Form.Label>

                            <Form.Control
                                type={u.type}
                                placeholder={`Nhập ${u.label.toLowerCase()}...`}
                                value={user[u.field] || ""}
                                onChange={e =>
                                    setUser({
                                        ...user,
                                        [u.field]: e.target.value
                                    })
                                }
                                disabled={loading}
                                required
                                className="rounded-3 py-2"
                            />
                        </Form.Group>
                    ))}


                    {loading ? (
                        <div className="text-center">
                            <MySpinner />
                        </div>
                    ) : (
                        <Button
                            variant="primary"
                            type="submit"
                            className="w-100 rounded-pill py-2 fw-bold fs-5 shadow-sm"
                        >
                            Đăng nhập
                        </Button>
                    )}
                </Form>


                <div className="text-center mt-4">
                    <span className="text-muted">
                        Chưa có tài khoản?{" "}
                    </span>

                    <Link
                        to={
                            q.get("next")
                                ? `/register?next=${encodeURIComponent(q.get("next"))}`
                                : "/register"
                        }
                        className="text-primary fw-bold text-decoration-none"
                    >
                        Đăng ký ngay
                    </Link>
                </div>
            </Card>
        </Container>
    );
};

export default Login;