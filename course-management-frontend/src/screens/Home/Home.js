import { useCallback, useEffect, useState } from "react";
import { Button, Card, Col, Form, Pagination, Row } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

import Apis, { endpoints } from "../../configs/Apis";
import "../../styles/Home.css";


const Home = () => {
    const [courses, setCourses] = useState([]);
    const [categories, setCategories] = useState([]);
    const [keyword, setKeyword] = useState("");
    const [categoryId, setCategoryId] = useState("");
    const [minPrice, setMinPrice] = useState("");
    const [maxPrice, setMaxPrice] = useState("");
    const [sort, setSort] = useState("");
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [totalRecords, setTotalRecords] = useState(0);

    const nav = useNavigate();


    const loadCategories = async () => {
        try {
            const res = await Apis.get(endpoints.publicCategories);
            setCategories(res.data || []);
        } catch(e) {
            console.error(e);
        }
    };


    const loadCourses = useCallback(async () => {
        try {
            const res = await Apis.get(endpoints.publicCourses, {
                params: {
                    page,
                    kw: keyword,
                    categoryId,
                    minPrice,
                    maxPrice,
                    sort
                }
            });

            setCourses(res.data.courses || []);
            setTotalPages(res.data.totalPages || 1);
            setTotalRecords(res.data.totalRecords || 0);

        } catch(e) {
            console.error(e);
        }
    }, [page, keyword, categoryId, minPrice, maxPrice, sort]);


    useEffect(() => {
        loadCategories();
    }, []);


    useEffect(() => {
        loadCourses();
    }, [loadCourses]);


    const search = e => {
        e.preventDefault();
        setPage(1);
    };


    const changePrice = value => {
        if(value === "free") {
            setMinPrice("");
            setMaxPrice("0");
        } else if(value === "low") {
            setMinPrice("");
            setMaxPrice("500000");
        } else if(value === "mid") {
            setMinPrice("500000");
            setMaxPrice("1000000");
        } else if(value === "high") {
            setMinPrice("1000000");
            setMaxPrice("");
        } else {
            setMinPrice("");
            setMaxPrice("");
        }

        setPage(1);
    };


    const formatPrice = price =>
        price
            ? `${price.toLocaleString("vi-VN")}đ`
            : "Miễn phí";


    return (
        <div className="cm-page">

            <section className="cm-section cm-home">

                <div className="cm-home-header">
                    <h1>Khóa học</h1>
                    <p>Tìm kiếm khóa học phù hợp</p>
                </div>


                <Form className="cm-home-search" onSubmit={search}>

                    <Form.Control
                        placeholder="Tìm kiếm khóa học..."
                        value={keyword}
                        onChange={e => {
                            setKeyword(e.target.value);
                            setPage(1);
                        }}
                    />

                    <Button type="submit">
                        Tìm kiếm
                    </Button>

                </Form>


                <div className="cm-filter-box">

                    <Form.Select
                        value={categoryId}
                        onChange={e => {
                            setCategoryId(e.target.value);
                            setPage(1);
                        }}
                    >
                        <option value="">
                            Tất cả danh mục
                        </option>

                        {categories.map(c => (
                            <option key={c.id} value={c.id}>
                                {c.name.replace("[SEED] ", "")}
                            </option>
                        ))}

                    </Form.Select>


                    <Form.Select
                        value={sort}
                        onChange={e => {
                            setSort(e.target.value);
                            setPage(1);
                        }}
                    >
                        <option value="">
                            Mặc định
                        </option>

                        <option value="priceAsc">
                            Giá thấp đến cao
                        </option>

                        <option value="priceDesc">
                            Giá cao đến thấp
                        </option>

                        <option value="nameAsc">
                            Tên A-Z
                        </option>

                    </Form.Select>


                    <Form.Select onChange={e => changePrice(e.target.value)}>

                        <option value="">
                            Tất cả giá
                        </option>

                        <option value="free">
                            Miễn phí
                        </option>

                        <option value="low">
                            Dưới 500.000đ
                        </option>

                        <option value="mid">
                            500.000đ - 1.000.000đ
                        </option>

                        <option value="high">
                            Trên 1.000.000đ
                        </option>

                    </Form.Select>

                </div>

                <p className="cm-result-count">
                    Tìm thấy {totalRecords} khóa học
                </p>


                <Row className="g-4">

                    {courses.length > 0 ? 
                        courses.map(course => (

                        <Col key={course.id} xs={12} md={6} lg={4}>

                            <Card className="cm-home-card h-100">

                                <img
                                    src={
                                        course.imageUrl ||
                                        "/images/course-default.jpg"
                                    }
                                    alt={course.name}
                                    className="cm-home-image"
                                />


                                <Card.Body>

                                    <span className="cm-course-tag">
                                        {
                                            course.categoryName?.replace(
                                                "[SEED] ",
                                                ""
                                            )
                                        }
                                    </span>


                                    <h3>
                                        {course.name}
                                    </h3>


                                    <p>
                                        {course.description}
                                    </p>


                                    <div className="cm-course-footer">

                                        <strong>
                                            {formatPrice(course.tuitionFee)}
                                        </strong>


                                        <Button
                                            onClick={() =>
                                                nav(`/courses/${course.id}`)
                                            }
                                        >
                                            Xem chi tiết
                                        </Button>

                                    </div>

                                </Card.Body>

                            </Card>

                        </Col>

                    )):

                    <div className="cm-empty">
                        Không tìm thấy khóa học phù hợp
                    </div>
                    
                    }

                </Row>


                <Pagination className="cm-home-pagination">

                    {[...Array(totalPages)].map((_, i) => (
                        <Pagination.Item
                            key={i}
                            active={page === i + 1}
                            onClick={() => setPage(i + 1)}
                        >
                            {i + 1}
                        </Pagination.Item>
                    ))}

                </Pagination>

            </section>

        </div>
    );
};


export default Home;