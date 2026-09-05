import { useContext, useEffect, useState } from "react";
import { Button, Container, Dropdown, Nav, Navbar, NavDropdown } from "react-bootstrap";
import { Link, useLocation, useNavigate } from "react-router-dom";
import cookies from "react-cookies";
import { MyUserContext } from "../configs/Contexts";
import Apis, { endpoints } from "../configs/Apis";
import "../styles/Header.css";

const Header = () => {
    const [user, dispatch] = useContext(MyUserContext);
    const [categories, setCategories] = useState([]);
    const location = useLocation();
    const nav = useNavigate();

    useEffect(() => {
        const loadCategories = async () => {
            try {
                const res = await Apis.get(endpoints.publicCategories);
                setCategories(Array.isArray(res.data) ? res.data : []);
            } catch (ex) {
                console.error("Load header categories error:", ex);
            }
        };

        loadCategories();
    }, []);

    const logout = () => {
        cookies.remove("token", { path: "/" });
        cookies.remove("user", { path: "/" });
    
        dispatch({
            type: "LOGOUT"
        });
    
        nav("/", { replace: true });
    };

    const goCategory = id => {
        nav(id ? `/?categoryId=${id}#courses` : "/#courses");

        setTimeout(() => {
            document.getElementById("courses")?.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        }, 100);
    };

    const initials = () => {
        const name = user?.fullName || user?.username || "U";
        return name.trim().split(/\s+/).slice(-2).map(x => x[0]).join("").toUpperCase();
    };

    const profile = () => (
        <button className="cm-profile" type="button">{initials()}</button>
    );

    const logoutItem = () => (
        <>
            <Dropdown.Divider />
            <Dropdown.Item className="cm-menu-logout" onClick={logout}>
                <span className="cm-menu-icon">↪</span>
                Đăng xuất
            </Dropdown.Item>
        </>
    );

    const studentMenu = () => (
        <>
            <Dropdown align="end">
                <Dropdown.Toggle variant="outline-primary" className="cm-role-button">
                    Học tập
                </Dropdown.Toggle>

                <Dropdown.Menu className="cm-account-menu">
                    <Dropdown.Item onClick={() => nav("/student")}>
                        <span className="cm-menu-icon">⌂</span>
                        Tổng quan
                    </Dropdown.Item>

                    <Dropdown.Item onClick={() => nav("/student/courses")}>
                        <span className="cm-menu-icon">▤</span>
                        Khóa học của tôi
                    </Dropdown.Item>

                    <Dropdown.Item onClick={() => nav("/student/assignments")}>
                        <span className="cm-menu-icon">✎</span>
                        Bài tập của tôi
                    </Dropdown.Item>

                    <Dropdown.Item onClick={() => nav("/student/schedule")}>
                        <span className="cm-menu-icon">◷</span>
                        Lịch học
                    </Dropdown.Item>

                    <Dropdown.Item onClick={() => nav("/student/payments")}>
                        <span className="cm-menu-icon">₫</span>
                        Thanh toán
                    </Dropdown.Item>

                    {logoutItem()}
                </Dropdown.Menu>
            </Dropdown>

            {profile()}
        </>
    );

    const teacherMenu = () => (
        <>
            <Dropdown align="end">
                <Dropdown.Toggle variant="outline-primary" className="cm-role-button">
                    Giảng dạy
                </Dropdown.Toggle>
    
                <Dropdown.Menu className="cm-account-menu">
                    <Dropdown.Item onClick={() => nav("/teacher/classes")}>
                        <span className="cm-menu-icon">▤</span>
                        Lớp học của tôi
                    </Dropdown.Item>
    
                    <Dropdown.Item onClick={() => nav("/teacher/grading")}>
                        <span className="cm-menu-icon">✓</span>
                        Chấm bài
                    </Dropdown.Item>
    
                    {logoutItem()}
                </Dropdown.Menu>
            </Dropdown>
    
            {profile()}
        </>
    );

    const standardMenu = () => (
        <>
            {user.role === "ADMIN" && (
                <Button variant="outline-primary" onClick={() => nav("/admin")}>
                    Dashboard
                </Button>
            )}

            {user.role === "MANAGER" && (
                <Button variant="outline-primary" onClick={() => nav("/manager")}>
                    Quản lý vận hành
                </Button>
            )}

            {user.role === "PARENT" && (
                <Button variant="outline-primary" onClick={() => nav("/parent")}>
                    Theo dõi học tập
                </Button>
            )}

            {profile()}

            <Button variant="link" className="cm-logout" onClick={logout}>
                Đăng xuất
            </Button>
        </>
    );

    const userActions = () => {
        if (!user)
            return (
                <>
                    <Button variant="link" className="cm-login-link" onClick={() => nav("/login")}>
                        Đăng nhập
                    </Button>

                    <Button onClick={() => nav("/register")}>Đăng ký</Button>
                </>
            );

        if (user.role === "STUDENT") return studentMenu();
        if (user.role === "TEACHER") return teacherMenu();

        return standardMenu();
    };

    return (
        <Navbar expand="md" className="cm-header" sticky="top">
            <Container fluid="xl" className="cm-header-inner">
                <Navbar.Brand as={Link} to="/" className="cm-logo">
                    <span className="cm-logo-mark">C</span>
                    <span>CourseHub</span>
                </Navbar.Brand>

                <Navbar.Toggle className="border-0 shadow-none" />

                <Navbar.Collapse>
                    <Nav className="mx-auto cm-main-nav">
                        <Nav.Link as={Link} to="/" className={location.pathname === "/" ? "active" : ""}>
                            Trang chủ
                        </Nav.Link>

                        <NavDropdown title="Khóa học" id="course-dropdown" className="cm-nav-dropdown">
                            <NavDropdown.Item onClick={() => goCategory(null)}>
                                Tất cả khóa học
                            </NavDropdown.Item>

                            {categories.length > 0 && <NavDropdown.Divider />}

                            {categories.map(category => (
                                <NavDropdown.Item key={category.id} onClick={() => goCategory(category.id)}>
                                    {category.name}
                                </NavDropdown.Item>
                            ))}
                        </NavDropdown>
                    </Nav>

                    <div className="cm-header-actions">{userActions()}</div>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    );
};

export default Header;